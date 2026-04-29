package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 月度销售数据 VO
 */
@Data
public class SalesMonthlyVO {
    /** 月份，如 "2024-01" */
    private String month;
    /** 销售额 */
    private BigDecimal salesAmount;
    /** 订单数量 */
    private Long orderCount;
    /** 发货金额 */
    private BigDecimal shipmentAmount;
    /** 产品数量 */
    private BigDecimal productQuantity;
}
