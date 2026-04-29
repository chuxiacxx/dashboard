package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.DataRow;
import com.fusemi.dashboard.dto.ImportResult;
import com.fusemi.dashboard.entity.*;
import com.fusemi.dashboard.repository.*;
import com.fusemi.dashboard.service.parser.CsvParser;
import com.fusemi.dashboard.service.parser.DataParser;
import com.fusemi.dashboard.service.parser.ExcelParser;
import com.fusemi.dashboard.service.parser.SalesOrderDetailRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    private final ExcelParser excelParser;
    private final CsvParser csvParser;
    private final SalesOrderDetailRowMapper salesOrderDetailRowMapper;
    private final SalesOrderDetailRepository salesOrderDetailRepository;

    // 维度表 Repository
    private final DimCustomerRepository dimCustomerRepository;
    private final DimProductRepository dimProductRepository;
    private final DimSalespersonRepository dimSalespersonRepository;

    // 订单主表/明细表 Repository
    private final SalesOrderMasterRepository salesOrderMasterRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;

    // 汇总表 Repository
    private final SalesMonthlySummaryRepository salesMonthlySummaryRepository;
    private final CustomerMonthlySummaryRepository customerMonthlySummaryRepository;
    private final ProductMonthlySummaryRepository productMonthlySummaryRepository;
    private final SalespersonMonthlySummaryRepository salespersonMonthlySummaryRepository;

    @Transactional
    public ImportResult importData(MultipartFile file, String dataType) {
        String filename = file.getOriginalFilename();
        log.info("开始导入数据: type={}, file={}", dataType, filename);

        // 选择解析器
        DataParser parser = selectParser(filename);
        if (parser == null) {
            return ImportResult.builder()
                    .dataType(dataType)
                    .fileName(filename)
                    .total(0)
                    .success(0)
                    .failed(0)
                    .errors(List.of("不支持的文件格式，仅支持 .xlsx, .xls, .csv"))
                    .build();
        }

        // 解析文件（订单数据支持多 Sheet）
        DataRow dataRow;
        try {
            if ("order".equals(dataType) && parser instanceof ExcelParser excelParserInstance) {
                dataRow = excelParserInstance.parseMultipleSheets(file);
            } else {
                dataRow = parser.parse(file);
            }
        } catch (IOException e) {
            log.error("文件解析失败", e);
            return ImportResult.builder()
                    .dataType(dataType)
                    .fileName(filename)
                    .total(0)
                    .success(0)
                    .failed(0)
                    .errors(List.of("文件解析失败: " + e.getMessage()))
                    .build();
        }

        // 按数据类型处理
        return switch (dataType) {
            case "sales" -> processSalesData(dataRow, filename);
            case "order" -> processOrderDetailData(dataRow, filename);
            default -> ImportResult.builder()
                    .dataType(dataType)
                    .fileName(filename)
                    .total(0)
                    .success(0)
                    .failed(0)
                    .errors(List.of("暂不支持的数据类型: " + dataType))
                    .build();
        };
    }

    private DataParser selectParser(String filename) {
        if (filename == null) {
            return null;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return excelParser;
        }
        if (lower.endsWith(".csv")) {
            return csvParser;
        }
        return null;
    }

    /**
     * 处理销售订单明细数据（增量更新）
     * 同时保存维度表、订单主表/明细表，并触发汇总更新
     */
    private ImportResult processOrderDetailData(DataRow dataRow, String filename) {
        List<String> errors = new ArrayList<>();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        // 用于收集需要更新的汇总维度
        Set<YearMonth> affectedMonths = new HashSet<>();

        int rowNum = 1;
        for (Map<String, String> row : dataRow.getRows()) {
            try {
                SalesOrderDetail detail = salesOrderDetailRowMapper.map(row);

                // 基本校验
                if (detail.getOrderNo() == null || detail.getOrderNo().isEmpty()) {
                    errors.add("第 " + rowNum + " 行: 订单编号为空");
                    skipped++;
                    rowNum++;
                    continue;
                }

                // 1. 保存/更新维度表数据
                saveOrUpdateDimCustomer(detail);
                saveOrUpdateDimProduct(detail);
                saveOrUpdateDimSalesperson(detail);

                // 2. 保存/更新订单主表
                SalesOrderMaster master = saveOrUpdateSalesOrderMaster(detail);

                // 3. 保存/更新订单明细表
                boolean isNewLine = saveOrUpdateSalesOrderLine(detail);

                // 4. 记录受影响的月份（用于后续汇总更新）
                if (master.getOrderCreateDate() != null) {
                    affectedMonths.add(new YearMonth(
                            master.getOrderCreateDate().getYear(),
                            master.getOrderCreateDate().getMonthValue()
                    ));
                }

                // 5. 保留原有 SalesOrderDetail 的保存逻辑（向后兼容）
                Optional<SalesOrderDetail> existingDetail = salesOrderDetailRepository
                        .findByOrderNoAndOrderLineNo(detail.getOrderNo(), detail.getOrderLineNo());

                if (existingDetail.isPresent()) {
                    SalesOrderDetail old = existingDetail.get();
                    detail.setId(old.getId());
                    detail.setCreateTime(old.getCreateTime());
                    salesOrderDetailRepository.save(detail);
                } else {
                    salesOrderDetailRepository.save(detail);
                }

                // 以 SalesOrderLine 的新增/更新作为计数依据
                if (isNewLine) {
                    created++;
                } else {
                    updated++;
                }

            } catch (Exception e) {
                errors.add("第 " + rowNum + " 行: " + e.getMessage());
                skipped++;
            }
            rowNum++;
        }

        // 6. 触发汇总数据更新
        for (YearMonth ym : affectedMonths) {
            try {
                updateMonthlySummaries(ym.year, ym.month);
            } catch (Exception e) {
                log.error("更新月度汇总失败: year={}, month={}", ym.year, ym.month, e);
                errors.add("汇总更新失败 (" + ym.year + "-" + ym.month + "): " + e.getMessage());
            }
        }

        log.info("订单明细导入完成: 新建={}, 更新={}, 跳过={}, 错误={}", created, updated, skipped, errors.size());

        return ImportResult.builder()
                .dataType("order")
                .fileName(filename)
                .total(dataRow.getRowCount())
                .success(created + updated)
                .failed(skipped)
                .created(created)
                .updated(updated)
                .errors(errors)
                .build();
    }

    // ==================== 维度表操作 ====================

    private void saveOrUpdateDimCustomer(SalesOrderDetail detail) {
        if (detail.getCustomerCode() == null || detail.getCustomerCode().isEmpty()) {
            return;
        }
        Optional<DimCustomer> existing = dimCustomerRepository.findByCustomerCode(detail.getCustomerCode());
        DimCustomer customer = existing.orElse(new DimCustomer());
        customer.setCustomerCode(detail.getCustomerCode());
        customer.setCustomerName(detail.getCustomerName());
        customer.setCustomerShortName(detail.getCustomerShortName());
        customer.setCustomerCategoryCode(detail.getCustomerCategoryCode());
        customer.setCustomerCategoryDesc(detail.getCustomerCategoryDesc());
        customer.setSalesRegionCode(detail.getSalesRegionCode());
        customer.setSalesRegionDesc(detail.getSalesRegionDesc());
        customer.setPaymentTerms(detail.getPaymentTerms());
        customer.setPaymentTermsDesc(detail.getPaymentTermsDesc());
        customer.setUpdateTime(LocalDateTime.now());
        if (customer.getCreateTime() == null) {
            customer.setCreateTime(LocalDateTime.now());
        }
        dimCustomerRepository.save(customer);
    }

    private void saveOrUpdateDimProduct(SalesOrderDetail detail) {
        if (detail.getProductCode() == null || detail.getProductCode().isEmpty()) {
            return;
        }
        Optional<DimProduct> existing = dimProductRepository.findByProductCode(detail.getProductCode());
        DimProduct product = existing.orElse(new DimProduct());
        product.setProductCode(detail.getProductCode());
        product.setProductName(detail.getProductName());
        product.setProductFullName(detail.getProductFullName());
        product.setSpecModel(detail.getSpecModel());
        product.setProductLineCode(detail.getProductLineCode());
        product.setProductLineDesc(detail.getProductLineDesc());
        product.setProductGroupCode(detail.getProductGroupCode());
        product.setProductGroupDesc(detail.getProductGroupDesc());
        product.setUnit(detail.getUnit());
        product.setUpdateTime(LocalDateTime.now());
        if (product.getCreateTime() == null) {
            product.setCreateTime(LocalDateTime.now());
        }
        dimProductRepository.save(product);
    }

    private void saveOrUpdateDimSalesperson(SalesOrderDetail detail) {
        if (detail.getSalespersonCode() == null || detail.getSalespersonCode().isEmpty()) {
            return;
        }
        Optional<DimSalesperson> existing = dimSalespersonRepository.findBySalespersonCode(detail.getSalespersonCode());
        DimSalesperson salesperson = existing.orElse(new DimSalesperson());
        salesperson.setSalespersonCode(detail.getSalespersonCode());
        salesperson.setSalespersonName(detail.getSalespersonName());
        salesperson.setSalesOrgCode(detail.getSalesOrgCode());
        salesperson.setSalesOrgDesc(detail.getSalesOrgDesc());
        salesperson.setSalesRegionCode(detail.getSalesRegionCode());
        salesperson.setSalesRegionDesc(detail.getSalesRegionDesc());
        salesperson.setUpdateTime(LocalDateTime.now());
        if (salesperson.getCreateTime() == null) {
            salesperson.setCreateTime(LocalDateTime.now());
        }
        dimSalespersonRepository.save(salesperson);
    }

    // ==================== 订单主表/明细表操作 ====================

    private SalesOrderMaster saveOrUpdateSalesOrderMaster(SalesOrderDetail detail) {
        Optional<SalesOrderMaster> existing = salesOrderMasterRepository.findByOrderNo(detail.getOrderNo());
        SalesOrderMaster master = existing.orElse(new SalesOrderMaster());

        master.setOrderNo(detail.getOrderNo());
        master.setOrderTypeCode(detail.getOrderTypeCode());
        master.setOrderTypeDesc(detail.getOrderTypeDesc());
        master.setOrderStatus(detail.getOrderStatus());
        master.setOrderCreateDate(detail.getOrderCreateDate());
        master.setCustomerCode(detail.getCustomerCode());
        master.setCustomerPoNo(detail.getCustomerPoNo());
        master.setCustomerPoDate(detail.getCustomerPoDate());
        master.setContractNo(detail.getContractNo());
        master.setCurrency(detail.getCurrency());
        master.setExchangeRate(detail.getExchangeRate());
        master.setReferenceCurrency(detail.getReferenceCurrency());
        master.setReferenceExchangeRate(detail.getReferenceExchangeRate());
        master.setPaymentTerms(detail.getPaymentTerms());
        master.setPaymentTermsDesc(detail.getPaymentTermsDesc());
        master.setSalesOrgCode(detail.getSalesOrgCode());
        master.setSalesOrgDesc(detail.getSalesOrgDesc());
        master.setSalesRegionCode(detail.getSalesRegionCode());
        master.setSalesRegionDesc(detail.getSalesRegionDesc());
        master.setDistributionChannelCode(detail.getDistributionChannelCode());
        master.setDistributionChannelDesc(detail.getDistributionChannelDesc());
        master.setMarketSegmentCode(detail.getMarketSegmentCode());
        master.setMarketSegmentDesc(detail.getMarketSegmentDesc());
        master.setSalespersonCode(detail.getSalespersonCode());
        master.setSalespersonName(detail.getSalespersonName());
        master.setSalesAssistant(detail.getSalesAssistant());
        master.setSalesAssistantName(detail.getSalesAssistantName());
        master.setFae(detail.getFae());
        master.setFaeName(detail.getFaeName());
        master.setEstimatedDeliveryDate(detail.getEstimatedDeliveryDate());
        master.setSupplyChainDeliveryDate(detail.getSupplyChainDeliveryDate());
        master.setHeaderRemark(detail.getHeaderRemark());
        master.setRejectReasonCode(detail.getRejectReasonCode());
        master.setRejectReasonDesc(detail.getRejectReasonDesc());
        master.setCreator(detail.getCreator());

        master.setUpdateTime(LocalDateTime.now());
        if (master.getCreateTime() == null) {
            master.setCreateTime(LocalDateTime.now());
        }

        return salesOrderMasterRepository.save(master);
    }

    private boolean saveOrUpdateSalesOrderLine(SalesOrderDetail detail) {
        Optional<SalesOrderLine> existing = salesOrderLineRepository
                .findByOrderNoAndOrderLineNo(detail.getOrderNo(), detail.getOrderLineNo());
        SalesOrderLine line = existing.orElse(new SalesOrderLine());
        boolean isNew = existing.isEmpty();

        line.setOrderNo(detail.getOrderNo());
        line.setOrderLineNo(detail.getOrderLineNo());
        line.setProductCode(detail.getProductCode());
        line.setProductName(detail.getProductName());
        line.setProductFullName(detail.getProductFullName());
        line.setSpecModel(detail.getSpecModel());
        line.setUnit(detail.getUnit());
        line.setOrderQuantity(detail.getOrderQuantity());
        line.setUnitPrice(detail.getUnitPrice());
        line.setUnitPriceNoTax(detail.getUnitPriceNoTax());
        line.setOrderAmountNoTax(detail.getOrderAmountNoTax());
        line.setOrderAmountTax(detail.getOrderAmountTax());
        line.setOrderTax(detail.getOrderTax());
        line.setShippedQuantity(detail.getShippedQuantity());
        line.setShippedAmountNoTax(detail.getShippedAmountNoTax());
        line.setShippedAmountTax(detail.getShippedAmountTax());
        line.setUnshippedQuantity(detail.getUnshippedQuantity());
        line.setUnshippedAmountNoTax(detail.getUnshippedAmountNoTax());
        line.setUnshippedAmountTax(detail.getUnshippedAmountTax());
        line.setInvoicedQuantity(detail.getInvoicedQuantity());
        line.setInvoicedAmountNoTax(detail.getInvoicedAmountNoTax());
        line.setInvoicedAmountTax(detail.getInvoicedAmountTax());
        line.setUninvoicedQuantity(detail.getUninvoicedQuantity());
        line.setUninvoicedAmountNoTax(detail.getUninvoicedAmountNoTax());
        line.setUninvoicedAmountTax(detail.getUninvoicedAmountTax());
        line.setOriginalQuantity(detail.getOriginalQuantity());
        line.setOriginalOrderNo(detail.getOriginalOrderNo());
        line.setReferenceDoc(detail.getReferenceDoc());
        line.setReferenceItem(detail.getReferenceItem());
        line.setOverDeliveryQuantity(detail.getOverDeliveryQuantity());
        line.setOverDeliveryAmount(detail.getOverDeliveryAmount());
        line.setIsOverDelivery(detail.getIsOverDelivery());
        line.setLineRemark(detail.getLineRemark());

        line.setUpdateTime(LocalDateTime.now());
        if (line.getCreateTime() == null) {
            line.setCreateTime(LocalDateTime.now());
        }

        salesOrderLineRepository.save(line);
        return isNew;
    }

    // ==================== 汇总数据更新 ====================

    private void updateMonthlySummaries(int year, int month) {
        log.info("更新月度汇总数据: year={}, month={}", year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        updateSalesMonthlySummary(year, month, startDate, endDate);
        updateCustomerMonthlySummary(year, month, startDate, endDate);
        updateProductMonthlySummary(year, month, startDate, endDate);
        updateSalespersonMonthlySummary(year, month, startDate, endDate);
    }

    private void updateSalesMonthlySummary(int year, int month, LocalDate startDate, LocalDate endDate) {
        Optional<SalesMonthlySummary> existing = salesMonthlySummaryRepository.findByYearAndMonth(year, month);
        SalesMonthlySummary summary = existing.orElse(new SalesMonthlySummary());

        summary.setYear(year);
        summary.setMonth(month);

        // 从订单数据计算汇总值
        BigDecimal salesAmount = salesOrderMasterRepository.sumOrderAmountByDateRange(startDate, endDate);
        BigDecimal shippedAmount = salesOrderMasterRepository.sumShippedAmountByDateRange(startDate, endDate);
        BigDecimal invoicedAmount = salesOrderMasterRepository.sumInvoicedAmountByDateRange(startDate, endDate);
        Long orderCount = salesOrderMasterRepository.countDistinctOrdersByDateRange(startDate, endDate);

        summary.setSalesAmount(salesAmount != null ? salesAmount : BigDecimal.ZERO);
        summary.setShippedAmount(shippedAmount != null ? shippedAmount : BigDecimal.ZERO);
        summary.setInvoicedAmount(invoicedAmount != null ? invoicedAmount : BigDecimal.ZERO);
        summary.setOrderCount(orderCount != null ? orderCount.intValue() : 0);

        // 统计订单行数
        List<SalesOrderMaster> ordersInMonth = salesOrderMasterRepository.findAll().stream()
                .filter(o -> o.getOrderCreateDate() != null
                        && !o.getOrderCreateDate().isBefore(startDate)
                        && !o.getOrderCreateDate().isAfter(endDate))
                .toList();
        int orderLineCount = 0;
        Set<String> customerCodes = new HashSet<>();
        Set<String> productCodes = new HashSet<>();
        for (SalesOrderMaster order : ordersInMonth) {
            List<SalesOrderLine> lines = salesOrderLineRepository.findByOrderNo(order.getOrderNo());
            orderLineCount += lines.size();
            if (order.getCustomerCode() != null) {
                customerCodes.add(order.getCustomerCode());
            }
            for (SalesOrderLine line : lines) {
                if (line.getProductCode() != null) {
                    productCodes.add(line.getProductCode());
                }
            }
        }
        summary.setOrderLineCount(orderLineCount);
        summary.setCustomerCount(customerCodes.size());
        summary.setProductCount(productCodes.size());

        // 计算平均订单金额
        if (summary.getOrderCount() > 0 && summary.getSalesAmount().compareTo(BigDecimal.ZERO) > 0) {
            summary.setAvgOrderAmount(summary.getSalesAmount()
                    .divide(BigDecimal.valueOf(summary.getOrderCount()), 2, RoundingMode.HALF_UP));
        } else {
            summary.setAvgOrderAmount(BigDecimal.ZERO);
        }

        summary.setUpdateTime(LocalDateTime.now());
        if (summary.getCreateTime() == null) {
            summary.setCreateTime(LocalDateTime.now());
        }

        salesMonthlySummaryRepository.save(summary);
    }

    private void updateCustomerMonthlySummary(int year, int month, LocalDate startDate, LocalDate endDate) {
        // 先清除该月已有数据（使用批量删除避免加载实体到持久化上下文）
        customerMonthlySummaryRepository.deleteByYearAndMonth(year, month);

        // 按客户分组统计
        Map<String, CustomerMonthlySummary> customerMap = new HashMap<>();

        List<SalesOrderMaster> ordersInMonth = salesOrderMasterRepository.findAll().stream()
                .filter(o -> o.getOrderCreateDate() != null
                        && !o.getOrderCreateDate().isBefore(startDate)
                        && !o.getOrderCreateDate().isAfter(endDate)
                        && (o.getRejectReasonCode() == null || o.getRejectReasonCode().isEmpty()))
                .toList();

        for (SalesOrderMaster order : ordersInMonth) {
            if (order.getCustomerCode() == null) {
                continue;
            }
            String customerCode = order.getCustomerCode();
            CustomerMonthlySummary summary = customerMap.computeIfAbsent(customerCode, k -> {
                CustomerMonthlySummary s = new CustomerMonthlySummary();
                s.setYear(year);
                s.setMonth(month);
                s.setCustomerCode(customerCode);
                s.setCustomerName(order.getCustomerName());
                s.setRegion(order.getSalesRegionDesc());
                s.setSalesAmount(BigDecimal.ZERO);
                s.setOrderCount(0);
                s.setProductCount(0);
                s.setCreateTime(LocalDateTime.now());
                return s;
            });

            List<SalesOrderLine> lines = salesOrderLineRepository.findByOrderNo(order.getOrderNo());
            BigDecimal orderAmount = lines.stream()
                    .map(SalesOrderLine::getOrderAmountTax)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.setSalesAmount(summary.getSalesAmount().add(orderAmount));
            summary.setOrderCount(summary.getOrderCount() + 1);

            Set<String> products = new HashSet<>();
            for (SalesOrderLine line : lines) {
                if (line.getProductCode() != null) {
                    products.add(line.getProductCode());
                }
            }
            summary.setProductCount(products.size());
            summary.setUpdateTime(LocalDateTime.now());
        }

        if (!customerMap.isEmpty()) {
            customerMonthlySummaryRepository.saveAll(customerMap.values());
        }
    }

    private void updateProductMonthlySummary(int year, int month, LocalDate startDate, LocalDate endDate) {
        // 先清除该月已有数据（使用批量删除避免加载实体到持久化上下文）
        productMonthlySummaryRepository.deleteByYearAndMonth(year, month);

        // 按产品分组统计
        Map<String, ProductMonthlySummary> productMap = new HashMap<>();

        List<SalesOrderMaster> ordersInMonth = salesOrderMasterRepository.findAll().stream()
                .filter(o -> o.getOrderCreateDate() != null
                        && !o.getOrderCreateDate().isBefore(startDate)
                        && !o.getOrderCreateDate().isAfter(endDate)
                        && (o.getRejectReasonCode() == null || o.getRejectReasonCode().isEmpty()))
                .toList();

        for (SalesOrderMaster order : ordersInMonth) {
            List<SalesOrderLine> lines = salesOrderLineRepository.findByOrderNo(order.getOrderNo());
            for (SalesOrderLine line : lines) {
                if (line.getProductCode() == null) {
                    continue;
                }
                String productCode = line.getProductCode();
                ProductMonthlySummary summary = productMap.computeIfAbsent(productCode, k -> {
                    ProductMonthlySummary s = new ProductMonthlySummary();
                    s.setYear(year);
                    s.setMonth(month);
                    s.setProductCode(productCode);
                    s.setProductName(line.getProductName());
                    // 从维度表获取产品线
                    Optional<DimProduct> dimProduct = dimProductRepository.findByProductCode(productCode);
                    s.setProductLine(dimProduct.map(DimProduct::getProductLineDesc).orElse(line.getProductFullName()));
                    s.setSalesQuantity(BigDecimal.ZERO);
                    s.setSalesAmount(BigDecimal.ZERO);
                    s.setOrderCount(0);
                    s.setCreateTime(LocalDateTime.now());
                    return s;
                });

                summary.setSalesQuantity(summary.getSalesQuantity().add(
                        line.getOrderQuantity() != null ? line.getOrderQuantity() : BigDecimal.ZERO));
                summary.setSalesAmount(summary.getSalesAmount().add(
                        line.getOrderAmountTax() != null ? line.getOrderAmountTax() : BigDecimal.ZERO));
                summary.setOrderCount(summary.getOrderCount() + 1);
                summary.setUpdateTime(LocalDateTime.now());
            }
        }

        if (!productMap.isEmpty()) {
            productMonthlySummaryRepository.saveAll(productMap.values());
        }
    }

    private void updateSalespersonMonthlySummary(int year, int month, LocalDate startDate, LocalDate endDate) {
        // 先清除该月已有数据（使用批量删除避免加载实体到持久化上下文）
        salespersonMonthlySummaryRepository.deleteByYearAndMonth(year, month);

        // 按销售员分组统计
        Map<String, SalespersonMonthlySummary> salespersonMap = new HashMap<>();

        List<SalesOrderMaster> ordersInMonth = salesOrderMasterRepository.findAll().stream()
                .filter(o -> o.getOrderCreateDate() != null
                        && !o.getOrderCreateDate().isBefore(startDate)
                        && !o.getOrderCreateDate().isAfter(endDate)
                        && (o.getRejectReasonCode() == null || o.getRejectReasonCode().isEmpty()))
                .toList();

        for (SalesOrderMaster order : ordersInMonth) {
            if (order.getSalespersonCode() == null) {
                continue;
            }
            String salespersonCode = order.getSalespersonCode();
            SalespersonMonthlySummary summary = salespersonMap.computeIfAbsent(salespersonCode, k -> {
                SalespersonMonthlySummary s = new SalespersonMonthlySummary();
                s.setYear(year);
                s.setMonth(month);
                s.setSalespersonCode(salespersonCode);
                s.setSalespersonName(order.getSalespersonName());
                s.setSalesRegionDesc(order.getSalesRegionDesc());
                s.setSalesAmount(BigDecimal.ZERO);
                s.setShippedAmount(BigDecimal.ZERO);
                s.setOrderCount(0);
                s.setTargetAmount(BigDecimal.ZERO);
                s.setCompletionRate(BigDecimal.ZERO);
                s.setCreateTime(LocalDateTime.now());
                return s;
            });

            List<SalesOrderLine> lines = salesOrderLineRepository.findByOrderNo(order.getOrderNo());
            BigDecimal orderAmount = lines.stream()
                    .map(SalesOrderLine::getOrderAmountTax)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal shippedAmount = lines.stream()
                    .map(SalesOrderLine::getShippedAmountTax)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            summary.setSalesAmount(summary.getSalesAmount().add(orderAmount));
            summary.setShippedAmount(summary.getShippedAmount().add(shippedAmount));
            summary.setOrderCount(summary.getOrderCount() + 1);
            summary.setUpdateTime(LocalDateTime.now());
        }

        // 计算达成率
        for (SalespersonMonthlySummary summary : salespersonMap.values()) {
            if (summary.getTargetAmount() != null && summary.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                summary.setCompletionRate(summary.getSalesAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(summary.getTargetAmount(), 2, RoundingMode.HALF_UP));
            }
        }

        if (!salespersonMap.isEmpty()) {
            salespersonMonthlySummaryRepository.saveAll(salespersonMap.values());
        }
    }

    private ImportResult processSalesData(DataRow dataRow, String filename) {
        return ImportResult.builder()
                .dataType("sales")
                .fileName(filename)
                .total(dataRow.getRowCount())
                .success(0)
                .failed(dataRow.getRowCount())
                .errors(List.of("sales 数据类型导入已废弃，请使用 order 类型导入订单数据"))
                .build();
    }

    // ==================== 辅助类 ====================

    private record YearMonth(int year, int month) {
    }
}
