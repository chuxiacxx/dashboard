package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product_monthly_summary",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year", "month", "product_code"}))
public class ProductMonthlySummary extends BaseEntity {

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "product_line", length = 50)
    private String productLine;

    @Column(name = "sales_quantity", precision = 38, scale = 2)
    private BigDecimal salesQuantity = BigDecimal.ZERO;

    @Column(name = "sales_amount", precision = 38, scale = 2)
    private BigDecimal salesAmount = BigDecimal.ZERO;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
