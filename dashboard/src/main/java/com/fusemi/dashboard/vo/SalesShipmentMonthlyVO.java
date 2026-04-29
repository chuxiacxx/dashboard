package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 月度发货数据 VO
 */
@Data
public class SalesShipmentMonthlyVO {
    /** 月份 */
    private String month;
    /** 发货金额 */
    private BigDecimal shipmentAmount;
    /** 订单数量 */
    private Long orderCount;
    /** 发货数量 */
    private BigDecimal shippedQuantity;
}
