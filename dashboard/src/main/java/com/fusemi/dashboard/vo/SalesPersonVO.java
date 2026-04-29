package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 销售员业绩 VO
 */
@Data
public class SalesPersonVO {
    /** 销售员姓名 */
    private String name;
    /** 实际销售金额 */
    private BigDecimal actualAmount;
    /** 目标金额 */
    private BigDecimal targetAmount;
    /** 已发货金额 */
    private BigDecimal shippedAmount;
    /** 完成率 */
    private BigDecimal completionRate;
    /** 排名 */
    private Integer rank;
}
