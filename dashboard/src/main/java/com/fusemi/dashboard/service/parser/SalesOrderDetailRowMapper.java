package com.fusemi.dashboard.service.parser;

import com.fusemi.dashboard.entity.SalesOrderDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 销售订单明细行映射器
 * 将 Excel 中文列名映射到 SalesOrderDetail 实体
 */
@Slf4j
@Component
public class SalesOrderDetailRowMapper {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    };

    /**
     * 将 Excel 行数据映射为实体
     */
    public SalesOrderDetail map(Map<String, String> row) {
        SalesOrderDetail data = new SalesOrderDetail();

        // 订单基本信息
        data.setOrderCreateDate(parseExcelDate(getValue(row, "订单创建时间")));
        data.setOrderNo(getValue(row, "订单编号"));
        data.setOrderTypeDesc(getValue(row, "订单类型描述"));
        data.setSalespersonName(getValue(row, "销售员姓名"));
        data.setCustomerName(getValue(row, "客户名称"));
        data.setContractNo(getValue(row, "销售合同号"));
        data.setCustomerPoNo(getValue(row, "客户PO号"));

        // 产品信息
        data.setProductName(getValue(row, "销货名称"));
        data.setSpecModel(getValue(row, "规格型号"));
        data.setProductCode(getValue(row, "产品编号"));
        data.setProductFullName(getValue(row, "产品名称"));
        data.setProductGroupDesc(getValue(row, "产品组描述"));
        data.setProductLineDesc(getValue(row, "产品线描述"));
        data.setProductLineCode(getValue(row, "所属产品线"));

        // 数量与金额
        data.setOrderQuantity(parseBigDecimal(getValue(row, "订单数量")));
        data.setUnitPrice(parseBigDecimal(getValue(row, "单价")));
        data.setOrderAmountTax(parseBigDecimal(getValue(row, "订单金额(含税)")));
        data.setOrderAmountNoTax(parseBigDecimal(getValue(row, "订单金额(不含税)")));
        data.setOrderTax(parseBigDecimal(getValue(row, "订单税额")));
        data.setUnitPriceNoTax(parseBigDecimal(getValue(row, "不含税单价")));
        data.setOriginalQuantity(parseBigDecimal(getValue(row, "原订单数量")));
        data.setUnit(getValue(row, "单位"));

        // 发货信息
        data.setEstimatedDeliveryDate(parseExcelDate(getValue(row, "预计交货日期")));
        data.setShippedQuantity(parseBigDecimal(getValue(row, "已发货数量")));
        data.setUnshippedQuantity(parseBigDecimal(getValue(row, "未发货数量")));
        data.setShippedAmountTax(parseBigDecimal(getValue(row, "已发货金额（含税）")));
        data.setShippedAmountNoTax(parseBigDecimal(getValue(row, "已发货金额（不含税）")));
        data.setUnshippedAmountTax(parseBigDecimal(getValue(row, "未发货金额(含税)")));
        data.setUnshippedAmountNoTax(parseBigDecimal(getValue(row, "未发货金额（不含税）")));

        // 开票信息
        data.setInvoicedQuantity(parseBigDecimal(getValue(row, "已开票数量")));
        data.setInvoicedAmountTax(parseBigDecimal(getValue(row, "已开票金额(含税)")));
        data.setInvoicedAmountNoTax(parseBigDecimal(getValue(row, "已开票金额(不含税)")));
        data.setUninvoicedQuantity(parseBigDecimal(getValue(row, "未开票数量")));
        data.setUninvoicedAmountTax(parseBigDecimal(getValue(row, "未开票金额(含税)")));
        data.setUninvoicedAmountNoTax(parseBigDecimal(getValue(row, "未开票金额(不含税)")));

        // 客户信息
        data.setCustomerCode(getValue(row, "客户编码"));
        data.setCustomerShortName(getValue(row, "客户简称"));
        data.setCustomerCategoryDesc(getValue(row, "客户分类描述"));
        data.setCustomerCategoryCode(getValue(row, "客户分类"));
        data.setCustomerMaterial(getValue(row, "客户物料"));

        // 销售组织信息
        data.setSalesRegionDesc(getValue(row, "销售地区描述"));
        data.setSalesRegionCode(getValue(row, "销售地区"));
        data.setMarketSegmentDesc(getValue(row, "所属细分市场描述"));
        data.setMarketSegmentCode(getValue(row, "所属细分市场"));
        data.setSalesOrgDesc(getValue(row, "销售组织描述"));
        data.setSalesOrgCode(getValue(row, "销售组织"));
        data.setDistributionChannelDesc(getValue(row, "分销渠道描述"));
        data.setDistributionChannelCode(getValue(row, "分销渠道"));
        data.setProductGroupCode(getValue(row, "产品组"));

        // 订单状态
        data.setRejectReasonCode(getValue(row, "拒绝原因"));
        data.setRejectReasonDesc(getValue(row, "拒绝原因描述"));
        data.setOrderStatus((data.getRejectReasonCode() == null || data.getRejectReasonCode().isEmpty()) ? 0 : 1);

        data.setIsOverDelivery(getValue(row, "是否过量交货"));
        data.setOverDeliveryQuantity(parseBigDecimal(getValue(row, "过量交货数量")));
        data.setOverDeliveryAmount(parseBigDecimal(getValue(row, "过量交货金额（含税）")));

        // 其他信息
        data.setOriginalOrderNo(getValue(row, "原T+订单号"));
        data.setOrderLineNo(getValue(row, "销售订单行号"));
        data.setOrderTypeCode(getValue(row, "订单类型"));
        data.setOrderReasonCode(getValue(row, "订单原因"));
        data.setOrderReasonDesc(getValue(row, "订单原因描述"));
        data.setCreator(getValue(row, "创建人"));
        data.setPaymentTerms(getValue(row, "付款条件"));
        data.setPaymentTermsDesc(getValue(row, "付款条件描述"));
        data.setCurrency(getValue(row, "币种"));
        data.setExchangeRate(parseBigDecimal(getValue(row, "汇率")));
        data.setSalespersonCode(getValue(row, "销售员"));
        data.setSalesAssistant(getValue(row, "销售助理"));
        data.setSalesAssistantName(getValue(row, "销售助理姓名"));
        data.setFae(getValue(row, "FAE"));
        data.setFaeName(getValue(row, "FAE姓名"));
        data.setLineRemark(getValue(row, "行备注"));
        data.setHeaderRemark(getValue(row, "抬头备注"));
        data.setReferenceDoc(getValue(row, "参考凭证"));
        data.setReferenceItem(getValue(row, "参考项目"));
        data.setReferenceCurrency(getValue(row, "参考凭证币种"));
        data.setReferenceExchangeRate(parseBigDecimal(getValue(row, "参考凭证汇率")));
        data.setSupplyChainDeliveryDate(parseExcelDate(getValue(row, "供应链建议交期")));
        data.setCustomerPoDate(parseExcelDate(getValue(row, "客户po日期")));

        // 确保行号不为空（用于唯一键）
        if (data.getOrderLineNo() == null || data.getOrderLineNo().isEmpty()) {
            data.setOrderLineNo("10");
        }

        return data;
    }

    /**
     * 从行数据中获取值
     */
    private String getValue(Map<String, String> row, String key) {
        if (row == null || key == null) {
            return null;
        }
        String value = row.get(key);
        return value != null && !value.trim().isEmpty() ? value.trim() : null;
    }

    /**
     * 解析 Excel 日期（支持数字格式和字符串格式）
     */
    private LocalDate parseExcelDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // 尝试解析为数字（Excel 日期格式）
        try {
            double excelDate = Double.parseDouble(value.replace(",", ""));
            // Excel 日期是从 1899-12-30 开始的天数（Excel 的日期系统）
            // 注意：Excel 有一个已知的 1900 年闰年 bug，但对于现代日期影响不大
            return LocalDate.of(1899, 12, 30).plusDays((long) excelDate);
        } catch (NumberFormatException e) {
            // 不是数字，尝试字符串格式
        }

        // 尝试字符串日期格式
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        log.warn("无法解析日期: {}", value);
        return null;
    }

    /**
     * 解析 BigDecimal
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "").replace(" ", ""));
        } catch (NumberFormatException e) {
            log.warn("无法解析数值: {}", value);
            return null;
        }
    }
}
