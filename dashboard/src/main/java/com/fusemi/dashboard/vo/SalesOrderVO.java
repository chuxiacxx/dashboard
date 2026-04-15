package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单数据 VO
 */
@Data
public class SalesOrderVO {
    private Long id;
    private String orderNo;
    private LocalDate orderDate;
    private String customerName;
    private String salesperson;
    private String source;
    private BigDecimal amount;
    private Integer status;
}
