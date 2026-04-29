package com.fusemi.dashboard.service;

import com.fusemi.dashboard.entity.*;
import com.fusemi.dashboard.repository.*;
import com.fusemi.dashboard.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务数据服务
 * 优先从汇总表查询数据，无数据时回退到从原表实时计算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataService {

    private final SalesTargetRepository salesTargetRepository;

    // 汇总表 Repository
    private final SalesMonthlySummaryRepository salesMonthlySummaryRepository;
    private final SalespersonMonthlySummaryRepository salespersonMonthlySummaryRepository;

    // ProductMonthlySummary 查询需要使用 Repository
    private final ProductMonthlySummaryRepository productMonthlySummaryRepository;

    // 维度表 Repository
    private final DimSalespersonRepository dimSalespersonRepository;

    // 实时查询回退 Repository
    private final SalesOrderMasterRepository salesOrderMasterRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;

    // ==================== 月度销售数据 ====================

    /**
     * 获取月度销售数据（用于销售额趋势图）
     * 优先从 SalesMonthlySummary 获取，无数据时回退到原表
     */
    public List<SalesMonthlyVO> getSalesMonthlyData(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<SalesMonthlySummary> summaryList = salesMonthlySummaryRepository.findByYearOrderByMonth(year);

        if (!summaryList.isEmpty()) {
            return buildSalesMonthlyDataFromSummary(year, summaryList);
        }

        // 回退到原表实时计算
        return buildSalesMonthlyDataFromRealtime(year);
    }

    /**
     * 获取指定日期范围内的月度销售数据
     */
    public List<SalesMonthlyVO> getSalesMonthlyData(LocalDate startDate, LocalDate endDate) {
        // 直接查询原表获取跨月数据
        List<Object[]> rawData = salesOrderMasterRepository.sumAmountByMonth(startDate, endDate);
        List<Object[]> orderData = salesOrderMasterRepository.countOrdersByMonth(startDate, endDate);

        List<SalesMonthlyVO> result = new ArrayList<>();

        // 生成月份列表（从startDate到endDate的所有月份）
        LocalDate current = startDate.withDayOfMonth(1);
        final LocalDate endMonth = endDate.withDayOfMonth(1);

        while (!current.isAfter(endMonth)) {
            final String monthKey = String.format("%d-%02d", current.getYear(), current.getMonthValue());
            final int currentYear = current.getYear();
            final int currentMonthValue = current.getMonthValue();
            SalesMonthlyVO vo = new SalesMonthlyVO();
            vo.setMonth(monthKey);

            // 查找该月份的数据
            rawData.stream()
                    .filter(row -> {
                        LocalDate date = (LocalDate) row[0];
                        return date.getYear() == currentYear && date.getMonthValue() == currentMonthValue;
                    })
                    .findFirst()
                    .ifPresent(row -> vo.setSalesAmount((BigDecimal) row[1]));

            orderData.stream()
                    .filter(row -> {
                        LocalDate date = (LocalDate) row[0];
                        return date.getYear() == currentYear && date.getMonthValue() == currentMonthValue;
                    })
                    .findFirst()
                    .ifPresent(row -> vo.setOrderCount((Long) row[1]));

            if (vo.getSalesAmount() == null) vo.setSalesAmount(BigDecimal.ZERO);
            if (vo.getOrderCount() == null) vo.setOrderCount(0L);

            result.add(vo);

            // 移动到下一个月
            current = current.plusMonths(1);
        }

        return result;
    }

    private List<SalesMonthlyVO> buildSalesMonthlyDataFromSummary(Integer year, List<SalesMonthlySummary> summaryList) {
        List<SalesMonthlyVO> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            final int m = month;
            SalesMonthlyVO vo = new SalesMonthlyVO();
            vo.setMonth(String.format("%d-%02d", year, month));

            summaryList.stream()
                    .filter(s -> s.getMonth() == m)
                    .findFirst()
                    .ifPresent(s -> {
                        vo.setSalesAmount(s.getSalesAmount());
                        vo.setOrderCount(s.getOrderCount() != null ? s.getOrderCount().longValue() : 0L);
                    });

            if (vo.getSalesAmount() == null) vo.setSalesAmount(BigDecimal.ZERO);
            if (vo.getOrderCount() == null) vo.setOrderCount(0L);

            result.add(vo);
        }
        return result;
    }

    private List<SalesMonthlyVO> buildSalesMonthlyDataFromRealtime(Integer year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Object[]> rawData = salesOrderMasterRepository.sumAmountByMonth(startDate, endDate);
        List<Object[]> orderData = salesOrderMasterRepository.countOrdersByMonth(startDate, endDate);

        List<SalesMonthlyVO> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            final int m = month;
            SalesMonthlyVO vo = new SalesMonthlyVO();
            vo.setMonth(String.format("%d-%02d", year, month));

            rawData.stream()
                    .filter(row -> {
                        LocalDate date = (LocalDate) row[0];
                        return date.getMonthValue() == m;
                    })
                    .findFirst()
                    .ifPresent(row -> vo.setSalesAmount((BigDecimal) row[1]));

            orderData.stream()
                    .filter(row -> {
                        LocalDate date = (LocalDate) row[0];
                        return date.getMonthValue() == m;
                    })
                    .findFirst()
                    .ifPresent(row -> vo.setOrderCount((Long) row[1]));

            if (vo.getSalesAmount() == null) vo.setSalesAmount(BigDecimal.ZERO);
            if (vo.getOrderCount() == null) vo.setOrderCount(0L);

            result.add(vo);
        }

        return result;
    }

    // ==================== 月度发货数据 ====================

    /**
     * 获取月度发货数据
     * 优先从 SalesMonthlySummary 获取，无数据时回退到原表
     */
    public List<SalesShipmentMonthlyVO> getShipmentMonthlyData(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<SalesMonthlySummary> summaryList = salesMonthlySummaryRepository.findByYearOrderByMonth(year);

        if (!summaryList.isEmpty()) {
            return buildShipmentMonthlyDataFromSummary(year, summaryList);
        }

        // 回退到原表实时计算
        return buildShipmentMonthlyDataFromRealtime(year);
    }

    /**
     * 获取指定日期范围内的月度发货数据
     */
    public List<SalesShipmentMonthlyVO> getShipmentMonthlyData(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = salesOrderMasterRepository.sumShipmentByMonth(startDate, endDate);

        List<SalesShipmentMonthlyVO> result = new ArrayList<>();

        // 生成月份列表（从startDate到endDate的所有月份）
        LocalDate current = startDate.withDayOfMonth(1);
        LocalDate endMonth = endDate.withDayOfMonth(1);

        while (!current.isAfter(endMonth)) {
            final String monthKey = String.format("%d-%02d", current.getYear(), current.getMonthValue());
            final int currentYear = current.getYear();
            final int currentMonthValue = current.getMonthValue();
            SalesShipmentMonthlyVO vo = new SalesShipmentMonthlyVO();
            vo.setMonth(monthKey);

            rawData.stream()
                    .filter(row -> {
                        LocalDate date = (LocalDate) row[0];
                        return date.getYear() == currentYear && date.getMonthValue() == currentMonthValue;
                    })
                    .findFirst()
                    .ifPresent(row -> {
                        vo.setShipmentAmount((BigDecimal) row[1]);
                        vo.setOrderCount((Long) row[2]);
                    });

            if (vo.getShipmentAmount() == null) vo.setShipmentAmount(BigDecimal.ZERO);
            if (vo.getOrderCount() == null) vo.setOrderCount(0L);

            result.add(vo);

            current = current.plusMonths(1);
        }

        return result;
    }

    private List<SalesShipmentMonthlyVO> buildShipmentMonthlyDataFromSummary(Integer year, List<SalesMonthlySummary> summaryList) {
        List<SalesShipmentMonthlyVO> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            final int m = month;
            SalesShipmentMonthlyVO vo = new SalesShipmentMonthlyVO();
            vo.setMonth(String.format("%d-%02d", year, month));

            summaryList.stream()
                    .filter(s -> s.getMonth() == m)
                    .findFirst()
                    .ifPresent(s -> {
                        vo.setShipmentAmount(s.getShippedAmount());
                        vo.setOrderCount(s.getOrderCount() != null ? s.getOrderCount().longValue() : 0L);
                    });

            if (vo.getShipmentAmount() == null) vo.setShipmentAmount(BigDecimal.ZERO);
            if (vo.getOrderCount() == null) vo.setOrderCount(0L);

            result.add(vo);
        }
        return result;
    }

    private List<SalesShipmentMonthlyVO> buildShipmentMonthlyDataFromRealtime(Integer year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Object[]> rawData = salesOrderMasterRepository.sumShipmentByMonth(startDate, endDate);

        List<SalesShipmentMonthlyVO> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            final int m = month;
            SalesShipmentMonthlyVO vo = new SalesShipmentMonthlyVO();
            vo.setMonth(String.format("%d-%02d", year, month));

            rawData.stream()
                    .filter(row -> {
                        LocalDate date = (LocalDate) row[0];
                        return date.getMonthValue() == m;
                    })
                    .findFirst()
                    .ifPresent(row -> {
                        vo.setShipmentAmount((BigDecimal) row[1]);
                        vo.setOrderCount((Long) row[2]);
                    });

            if (vo.getShipmentAmount() == null) vo.setShipmentAmount(BigDecimal.ZERO);
            if (vo.getOrderCount() == null) vo.setOrderCount(0L);

            result.add(vo);
        }

        return result;
    }

    // ==================== 产品排行 ====================

    /**
     * 获取产品销量排行
     * 优先从 ProductMonthlySummary 获取，无数据时回退到原表
     */
    public List<SalesProductVO> getProductRanking(Integer year, Integer month, Integer limit) {
        if (year != null && month != null) {
            List<ProductMonthlySummary> summaryList = productMonthlySummaryRepository.findByYearAndMonth(year, month);
            if (!summaryList.isEmpty()) {
                return buildProductRankingFromSummary(summaryList, limit);
            }
        }

        // 回退到原表实时计算
        return buildProductRankingFromRealtime(year, month, limit);
    }

    /**
     * 获取指定日期范围内的产品销量排行
     */
    public List<SalesProductVO> getProductRanking(LocalDate startDate, LocalDate endDate, Integer limit) {
        List<Object[]> rawData = salesOrderMasterRepository.sumByProduct(startDate, endDate);

        List<SalesProductVO> result = new ArrayList<>();
        int rank = 1;

        for (Object[] row : rawData) {
            if (result.size() >= limit) break;

            SalesProductVO vo = new SalesProductVO();
            vo.setProductName((String) row[0]);
            vo.setQuantity((BigDecimal) row[1]);
            vo.setSalesAmount((BigDecimal) row[2]);
            vo.setRank(rank++);

            result.add(vo);
        }

        return result;
    }

    private List<SalesProductVO> buildProductRankingFromSummary(List<ProductMonthlySummary> summaryList, Integer limit) {
        List<SalesProductVO> result = new ArrayList<>();
        int rank = 1;

        for (ProductMonthlySummary s : summaryList) {
            if (result.size() >= limit) break;

            SalesProductVO vo = new SalesProductVO();
            vo.setProductName(s.getProductName());
            vo.setQuantity(s.getSalesQuantity());
            vo.setSalesAmount(s.getSalesAmount());
            vo.setRank(rank++);

            result.add(vo);
        }

        return result;
    }

    private List<SalesProductVO> buildProductRankingFromRealtime(Integer year, Integer month, Integer limit) {
        LocalDate startDate;
        LocalDate endDate;

        if (year != null && month != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if (year != null) {
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        } else {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        List<Object[]> rawData = salesOrderMasterRepository.sumByProduct(startDate, endDate);

        List<SalesProductVO> result = new ArrayList<>();
        int rank = 1;

        for (Object[] row : rawData) {
            if (result.size() >= limit) break;

            SalesProductVO vo = new SalesProductVO();
            vo.setProductName((String) row[0]);
            vo.setQuantity((BigDecimal) row[1]);
            vo.setSalesAmount((BigDecimal) row[2]);
            vo.setRank(rank++);

            result.add(vo);
        }

        return result;
    }

    // ==================== 销售员排行 ====================

    /**
     * 获取销售员业绩排行
     * 优先从 SalespersonMonthlySummary 获取，无数据时回退到原表
     */
    public List<SalesPersonVO> getSalespersonRanking(Integer year, Integer month) {
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        List<SalespersonMonthlySummary> summaryList = salespersonMonthlySummaryRepository
                .findByYearAndMonthOrderBySalesAmountDesc(year, month);

        if (!summaryList.isEmpty()) {
            return buildSalespersonRankingFromSummary(summaryList);
        }

        // 回退到原表实时计算
        return buildSalespersonRankingFromRealtime(year, month);
    }

    /**
     * 获取指定日期范围内的销售员业绩排行
     */
    public List<SalesPersonVO> getSalespersonRanking(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = salesOrderMasterRepository.sumBySalesperson(startDate, endDate);

        List<SalesPersonVO> result = new ArrayList<>();
        int rank = 1;

        for (Object[] row : rawData) {
            String name = (String) row[0];
            BigDecimal actualAmount = (BigDecimal) row[1];
            BigDecimal shippedAmount = (BigDecimal) row[2];

            SalesPersonVO vo = new SalesPersonVO();
            vo.setName(name);
            vo.setActualAmount(actualAmount);
            vo.setTargetAmount(BigDecimal.ZERO);
            vo.setShippedAmount(shippedAmount);
            vo.setCompletionRate(BigDecimal.ZERO);
            vo.setRank(rank++);

            result.add(vo);
        }

        return result;
    }

    private List<SalesPersonVO> buildSalespersonRankingFromSummary(List<SalespersonMonthlySummary> summaryList) {
        List<SalesPersonVO> result = new ArrayList<>();
        int rank = 1;

        for (SalespersonMonthlySummary s : summaryList) {
            SalesPersonVO vo = new SalesPersonVO();
            vo.setName(s.getSalespersonName());
            vo.setActualAmount(s.getSalesAmount());
            vo.setTargetAmount(s.getTargetAmount());
            vo.setShippedAmount(s.getShippedAmount());

            BigDecimal targetAmount = s.getTargetAmount() != null ? s.getTargetAmount() : BigDecimal.ZERO;
            BigDecimal actualAmount = s.getSalesAmount() != null ? s.getSalesAmount() : BigDecimal.ZERO;

            if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                vo.setCompletionRate(actualAmount
                        .divide(targetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                vo.setCompletionRate(BigDecimal.ZERO);
            }
            vo.setRank(rank++);

            result.add(vo);
        }

        return result;
    }

    private List<SalesPersonVO> buildSalespersonRankingFromRealtime(Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Object[]> rawData = salesOrderMasterRepository.sumBySalesperson(startDate, endDate);
        List<SalesTarget> targets = salesTargetRepository.findByYearAndMonth(year, month);

        List<SalesPersonVO> result = new ArrayList<>();
        int rank = 1;

        for (Object[] row : rawData) {
            String name = (String) row[0];
            BigDecimal actualAmount = (BigDecimal) row[1];
            BigDecimal shippedAmount = (BigDecimal) row[2];

            BigDecimal targetAmount = targets.stream()
                    .filter(t -> name.equals(t.getSalesperson()))
                    .findFirst()
                    .map(SalesTarget::getTargetAmount)
                    .orElse(BigDecimal.ZERO);

            SalesPersonVO vo = new SalesPersonVO();
            vo.setName(name);
            vo.setActualAmount(actualAmount);
            vo.setTargetAmount(targetAmount);
            vo.setShippedAmount(shippedAmount);

            if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                vo.setCompletionRate(actualAmount
                        .divide(targetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                vo.setCompletionRate(BigDecimal.ZERO);
            }
            vo.setRank(rank++);

            result.add(vo);
        }

        return result;
    }

    // ==================== 地区统计 ====================

    /**
     * 获取地区销售统计
     * 优先从 SalespersonMonthlySummary 聚合获取，无数据时回退到原表
     */
    public List<SalesRegionVO> getRegionStats(Integer year, Integer month) {
        LocalDate startDate;
        LocalDate endDate;

        if (year != null && month != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if (year != null) {
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        } else {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        }

        // 尝试从 SalespersonMonthlySummary 获取
        if (year != null && month != null) {
            List<Object[]> summaryData = salespersonMonthlySummaryRepository.sumByRegion(year, month);
            if (!summaryData.isEmpty()) {
                return buildRegionStatsFromSummary(summaryData);
            }
        }

        // 回退到原表实时计算
        return buildRegionStatsFromRealtime(startDate, endDate);
    }

    /**
     * 获取指定日期范围内的地区销售统计
     */
    public List<SalesRegionVO> getRegionStats(LocalDate startDate, LocalDate endDate) {
        return buildRegionStatsFromRealtime(startDate, endDate);
    }

    private List<SalesRegionVO> buildRegionStatsFromSummary(List<Object[]> summaryData) {
        BigDecimal totalAmount = summaryData.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesRegionVO> result = new ArrayList<>();

        for (Object[] row : summaryData) {
            SalesRegionVO vo = new SalesRegionVO();
            String regionName = (String) row[0];
            vo.setRegion(regionName != null && !regionName.isEmpty() ? regionName : "未分类");
            vo.setSalesAmount((BigDecimal) row[1]);
            // 汇总表没有订单数，设为0
            vo.setOrderCount(0L);

            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                vo.setPercentage(vo.getSalesAmount()
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                vo.setPercentage(BigDecimal.ZERO);
            }

            result.add(vo);
        }

        return result;
    }

    private List<SalesRegionVO> buildRegionStatsFromRealtime(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = salesOrderMasterRepository.sumByRegion(startDate, endDate);

        BigDecimal totalAmount = rawData.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesRegionVO> result = new ArrayList<>();

        for (Object[] row : rawData) {
            SalesRegionVO vo = new SalesRegionVO();
            String regionName = (String) row[0];
            vo.setRegion(regionName != null && !regionName.isEmpty() ? regionName : "未分类");
            vo.setSalesAmount((BigDecimal) row[1]);
            vo.setOrderCount((Long) row[2]);

            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                vo.setPercentage(vo.getSalesAmount()
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                vo.setPercentage(BigDecimal.ZERO);
            }

            result.add(vo);
        }

        return result;
    }

    // ==================== 辅助数据 ====================

    /**
     * 获取可用年份列表
     * 优先从 SalesMonthlySummary 获取，无数据时回退到原表
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = salesMonthlySummaryRepository.findAllYears();
        if (!years.isEmpty()) {
            return years;
        }

        years = salesOrderMasterRepository.findAllYears();
        if (years.isEmpty()) {
            years.add(LocalDate.now().getYear());
        }
        return years;
    }

    /**
     * 获取销售员列表
     * 优先从 DimSalespersonRepository 获取，无数据时回退到原表
     */
    public List<String> getSalespersonList() {
        List<String> list = dimSalespersonRepository.findAllSalespersonNames();
        if (!list.isEmpty()) {
            return list;
        }

        return salesOrderMasterRepository.findAllSalespersons();
    }

    /**
     * 获取地区列表
     * 优先从 DimSalespersonRepository 获取，无数据时回退到原表
     */
    public List<String> getRegionList() {
        List<String> list = dimSalespersonRepository.findAllRegions();
        if (!list.isEmpty()) {
            return list;
        }

        return salesOrderMasterRepository.findAllRegions();
    }

    // ==================== 订单跟踪数据 ====================

    /**
     * 获取最近的订单列表
     */
    public List<OrderTrackingVO> getRecentOrders(LocalDate startDate, LocalDate endDate) {
        List<SalesOrderMaster> orders = salesOrderMasterRepository.findRecentOrders(startDate, endDate);
        List<OrderTrackingVO> result = new ArrayList<>();

        for (SalesOrderMaster order : orders) {
            OrderTrackingVO vo = new OrderTrackingVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setCustomerName(order.getCustomerName());
            vo.setCreateDate(order.getOrderCreateDate());
            vo.setStatus(determineOrderStatus(order));

            // 计算订单金额
            BigDecimal amount = salesOrderLineRepository.sumOrderAmountByOrderNo(order.getOrderNo());
            vo.setAmount(amount != null ? amount : BigDecimal.ZERO);

            result.add(vo);
        }

        return result;
    }

    /**
     * 获取超期订单列表
     */
    public List<OrderTrackingVO> getOverdueOrders(LocalDate startDate, LocalDate endDate) {
        List<SalesOrderMaster> orders = salesOrderMasterRepository.findOverdueOrders(startDate, endDate);
        List<OrderTrackingVO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (SalesOrderMaster order : orders) {
            OrderTrackingVO vo = new OrderTrackingVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setCustomerName(order.getCustomerName());
            vo.setCreateDate(order.getOrderCreateDate());
            vo.setStatus("超期");

            // 计算超期天数
            if (order.getEstimatedDeliveryDate() != null) {
                vo.setOverdueDays((int) java.time.temporal.ChronoUnit.DAYS.between(
                        order.getEstimatedDeliveryDate(), today));
            } else {
                vo.setOverdueDays(0);
            }

            // 计算订单金额
            BigDecimal amount = salesOrderLineRepository.sumOrderAmountByOrderNo(order.getOrderNo());
            vo.setAmount(amount != null ? amount : BigDecimal.ZERO);

            result.add(vo);
        }

        return result;
    }

    private String determineOrderStatus(SalesOrderMaster order) {
        // 根据发货和开票情况判断状态
        // 这里简化处理，实际应该查询订单明细
        if (order.getRejectReasonCode() != null && !order.getRejectReasonCode().isEmpty()) {
            return "已拒绝";
        }
        return "进行中";
    }
}
