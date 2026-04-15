package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 发票数据 VO
 */
@Data
public class SalesInvoiceVO {
    private Long id;
    private String invoiceNo;
    private String orderNo;
    private String customerName;
    private LocalDate invoiceDate;
    private String type;
    private BigDecimal amount;
    private Integer status;
}
