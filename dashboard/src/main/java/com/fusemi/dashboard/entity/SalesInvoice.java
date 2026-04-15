package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_invoice")
public class SalesInvoice extends BaseEntity {

    private String invoiceNo;

    private String orderNo;

    private String customerName;

    private LocalDate invoiceDate;

    private String type; // 专用发票/普通发票

    private BigDecimal amount;

    private Integer status; // 0=未开, 1=已开, 2=已收
}
