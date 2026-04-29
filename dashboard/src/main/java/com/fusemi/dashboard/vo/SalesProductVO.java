package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 产品销售排行 VO
 */
@Data
public class SalesProductVO {
    /** 产品名称 */
    private String productName;
    /** 规格型号 */
    private String specModel;
    /** 销量 */
    private BigDecimal quantity;
    /** 销售金额 */
    private BigDecimal salesAmount;
    /** 排名 */
    private Integer rank;
}
