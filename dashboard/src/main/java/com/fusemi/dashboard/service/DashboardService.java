package com.fusemi.dashboard.service;

import com.fusemi.dashboard.entity.SalesMonthlySummary;
import com.fusemi.dashboard.entity.SalespersonMonthlySummary;
import com.fusemi.dashboard.repository.SalesMonthlySummaryRepository;
import com.fusemi.dashboard.repository.SalesOrderDetailRepository;
import com.fusemi.dashboard.repository.SalesOrderMasterRepository;
import com.fusemi.dashboard.repository.SalesTargetRepository;
import com.fusemi.dashboard.repository.SalespersonMonthlySummaryRepository;
import com.fusemi.dashboard.vo.DashboardSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 仪表盘服务
 * 优先从汇总表查询数据，提高性能；无数据时回退到实时计算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SalesOrderDetailRepository salesOrderDetailRepository;
    private final SalesTargetRepository salesTargetRepository;
    private final SalesMonthlySummaryRepository salesMonthlySummaryRepository;
    private final SalespersonMonthlySummaryRepository salespersonMonthlySummaryRepository;
    private final SalesOrderMasterRepository salesOrderMasterRepository;

    /**
     * 获取仪表盘汇总数据
     */
    public DashboardSummaryVO getSummary() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        DashboardSummaryVO vo = new DashboardSummaryVO();

        try {
            // 本月销售额（排除有拒绝原因的订单）
            BigDecimal salesAmount = salesOrderDetailRepository.sumOrderAmountByDateRange(startOfMonth, now);
            vo.setSalesAmount(salesAmount != null ? salesAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            log.error("Error getting sales amount", e);
            vo.setSalesAmount(BigDecimal.ZERO);
        }

        try {
            // 本月出货金额
            BigDecimal shipmentAmount = salesOrderDetailRepository.sumShippedAmountByDateRange(startOfMonth, now);
            vo.setShipmentAmount(shipmentAmount != null ? shipmentAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            log.error("Error getting shipment amount", e);
            vo.setShipmentAmount(BigDecimal.ZERO);
        }

        try {
            // 本月订单金额和数量
            BigDecimal orderAmount = salesOrderDetailRepository.sumOrderAmountByDateRange(startOfMonth, now);
            vo.setOrderAmount(orderAmount != null ? orderAmount : BigDecimal.ZERO);
            Long orderCount = salesOrderDetailRepository.countDistinctOrdersByDateRange(startOfMonth, now);
            vo.setOrderCount(orderCount != null ? orderCount : 0L);
        } catch (Exception e) {
            log.error("Error getting order data", e);
            vo.setOrderAmount(BigDecimal.ZERO);
            vo.setOrderCount(0L);
        }

        try {
            // 本月开票金额
            BigDecimal invoiceAmount = salesOrderDetailRepository.sumInvoicedAmountByDateRange(startOfMonth, now);
            vo.setInvoiceAmount(invoiceAmount != null ? invoiceAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            log.error("Error getting invoice amount", e);
            vo.setInvoiceAmount(BigDecimal.ZERO);
        }

        // 销售目标和完成率（从配置表读取）
        try {
            BigDecimal salesTarget = salesTargetRepository.sumTargetAmountByYearMonth(now.getYear(), now.getMonthValue());
            if (salesTarget == null || salesTarget.compareTo(BigDecimal.ZERO) == 0) {
                // 如果没有配置，使用上月销售额的 110% 作为默认目标
                BigDecimal lastMonthAmount = salesOrderDetailRepository.sumOrderAmountByDateRange(startOfLastMonth, endOfLastMonth);
                salesTarget = lastMonthAmount != null ? lastMonthAmount.multiply(new BigDecimal("1.1")) : new BigDecimal("1000000");
            }
            vo.setSalesTarget(salesTarget.setScale(2, RoundingMode.HALF_UP));

            BigDecimal completionRate = BigDecimal.ZERO;
            if (vo.getSalesTarget().compareTo(BigDecimal.ZERO) > 0) {
                completionRate = vo.getSalesAmount()
                        .divide(vo.getSalesTarget(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            vo.setSalesCompletionRate(completionRate);
        } catch (Exception e) {
            log.error("Error getting sales target", e);
            vo.setSalesTarget(new BigDecimal("1000000"));
            vo.setSalesCompletionRate(BigDecimal.ZERO);
        }

        // 计算环比增长率
        try {
            BigDecimal lastMonthAmount = salesOrderDetailRepository.sumOrderAmountByDateRange(startOfLastMonth, endOfLastMonth);
            if (lastMonthAmount != null && lastMonthAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growthRate = vo.getSalesAmount()
                        .subtract(lastMonthAmount)
                        .divide(lastMonthAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                vo.setSalesGrowthRate(growthRate);
            } else {
                vo.setSalesGrowthRate(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            log.error("Error calculating growth rate", e);
            vo.setSalesGrowthRate(BigDecimal.ZERO);
        }

        return vo;
    }
}
