package com.fusemi.dashboard.service;

import com.fusemi.dashboard.repository.*;
import com.fusemi.dashboard.vo.DashboardSummaryVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * 仪表盘服务
 */
@Service
public class DashboardService {

    private final SalesDataRepository salesDataRepository;
    private final SalesShipmentRepository shipmentRepository;
    private final SalesOrderRepository orderRepository;
    private final SalesInvoiceRepository invoiceRepository;
    private final SalesDealRepository dealRepository;
    private final SalesCustomerRepository customerRepository;

    public DashboardService(SalesDataRepository salesDataRepository,
                          SalesShipmentRepository shipmentRepository,
                          SalesOrderRepository orderRepository,
                          SalesInvoiceRepository invoiceRepository,
                          SalesDealRepository dealRepository,
                          SalesCustomerRepository customerRepository) {
        this.salesDataRepository = salesDataRepository;
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.dealRepository = dealRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * 获取仪表盘汇总数据
     */
    public DashboardSummaryVO getSummary() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        DashboardSummaryVO vo = new DashboardSummaryVO();

        try {
            // 销售额
            BigDecimal salesAmount = salesDataRepository.sumAmountByDateRange(startOfMonth, now);
            vo.setSalesAmount(salesAmount != null ? salesAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            System.err.println("Error getting sales amount: " + e.getMessage());
            vo.setSalesAmount(BigDecimal.ZERO);
        }

        try {
            // 出货金额
            BigDecimal shipmentAmount = shipmentRepository.sumAmountByDateRange(startOfMonth, now);
            vo.setShipmentAmount(shipmentAmount != null ? shipmentAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            System.err.println("Error getting shipment amount: " + e.getMessage());
            vo.setShipmentAmount(BigDecimal.ZERO);
        }

        try {
            // 订单金额和数量
            BigDecimal orderAmount = orderRepository.sumAmountByDateRange(startOfMonth, now);
            vo.setOrderAmount(orderAmount != null ? orderAmount : BigDecimal.ZERO);
            Long orderCount = orderRepository.countByDateRange(startOfMonth, now);
            vo.setOrderCount(orderCount != null ? orderCount : 0L);
        } catch (Exception e) {
            System.err.println("Error getting order data: " + e.getMessage());
            vo.setOrderAmount(BigDecimal.ZERO);
            vo.setOrderCount(0L);
        }

        try {
            // 开票金额
            BigDecimal invoiceAmount = invoiceRepository.sumAmountByDateRange(startOfMonth, now);
            vo.setInvoiceAmount(invoiceAmount != null ? invoiceAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            System.err.println("Error getting invoice amount: " + e.getMessage());
            vo.setInvoiceAmount(BigDecimal.ZERO);
        }

        try {
            // 成交金额
            BigDecimal dealAmount = dealRepository.sumAmountByDateRange(startOfMonth, now);
            vo.setDealAmount(dealAmount != null ? dealAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            System.err.println("Error getting deal amount: " + e.getMessage());
            vo.setDealAmount(BigDecimal.ZERO);
        }

        try {
            // 客户数
            Long newCustomerCount = customerRepository.countByIsNewAndDateRange(1, startOfMonth, now);
            Long oldCustomerCount = customerRepository.countByIsNewAndDateRange(0, startOfMonth, now);
            vo.setNewCustomerCount(newCustomerCount != null ? newCustomerCount : 0L);
            vo.setOldCustomerCount(oldCustomerCount != null ? oldCustomerCount : 0L);
        } catch (Exception e) {
            System.err.println("Error getting customer count: " + e.getMessage());
            vo.setNewCustomerCount(0L);
            vo.setOldCustomerCount(0L);
        }

        // 销售目标和完成率（固定值或可配置）
        vo.setSalesTarget(new BigDecimal("1200000")); // 120万目标
        BigDecimal completionRate = BigDecimal.ZERO;
        if (vo.getSalesTarget().compareTo(BigDecimal.ZERO) > 0) {
            completionRate = vo.getSalesAmount()
                    .divide(vo.getSalesTarget(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        vo.setSalesCompletionRate(completionRate);

        return vo;
    }
}
