package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 销售地区业绩 VO
 */
@Data
public class SalesRegionVO {
    /** 地区名称 */
    private String region;
    /** 销售金额 */
    private BigDecimal salesAmount;
    /** 订单数量 */
    private Long orderCount;
    /** 占比 */
    private BigDecimal percentage;
}
