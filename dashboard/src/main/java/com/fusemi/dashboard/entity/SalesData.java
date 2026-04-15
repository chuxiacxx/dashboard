package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_data")
public class SalesData extends BaseEntity {

    private LocalDate saleDate;

    private String region;

    private String salesperson;

    private String productName;

    private BigDecimal amount;

    private Integer quantity;

    private BigDecimal unitPrice;

    private String customerName;

    private String orderNo;

    private String status; // 已完成/pending/cancelled
}
