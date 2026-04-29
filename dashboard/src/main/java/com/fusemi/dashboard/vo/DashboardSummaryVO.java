package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 仪表盘汇总数据 VO
 */
@Data
public class DashboardSummaryVO {
    /** 销售额 */
    private BigDecimal salesAmount;
    /** 出货金额 */
    private BigDecimal shipmentAmount;
    /** 新增订单金额 */
    private BigDecimal orderAmount;
    /** 新增订单数 */
    private Long orderCount;
    /** 开票金额 */
    private BigDecimal invoiceAmount;
    /** 成交金额 */
    private BigDecimal dealAmount;
    /** 新客户数 */
    private Long newCustomerCount;
    /** 老客户数 */
    private Long oldCustomerCount;
    /** 销售目标 */
    private BigDecimal salesTarget;
    /** 销售完成率 */
    private BigDecimal salesCompletionRate;
    /** 销售额环比增长率 */
    private BigDecimal salesGrowthRate;
    /** 出货环比增长率 */
    private BigDecimal shipmentGrowthRate;
    /** 订单环比增长率 */
    private BigDecimal orderGrowthRate;
    /** 开票环比增长率 */
    private BigDecimal invoiceGrowthRate;
}
