package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成交数据 VO
 */
@Data
public class SalesDealVO {
    private Long id;
    private LocalDate dealDate;
    private String productName;
    private BigDecimal amount;
    private Integer quantity;
    private BigDecimal conversionRate;
    private String stage;
}
