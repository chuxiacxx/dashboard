package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销售数据 VO
 */
@Data
public class SalesDataVO {
    private Long id;
    private LocalDate saleDate;
    private String region;
    private String salesperson;
    private String productName;
    private BigDecimal amount;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String customerName;
    private String orderNo;
    private String status;
}
