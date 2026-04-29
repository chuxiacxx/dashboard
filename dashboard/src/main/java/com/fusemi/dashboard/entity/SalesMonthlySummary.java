package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_monthly_summary",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year", "month"}))
public class SalesMonthlySummary extends BaseEntity {

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "sales_amount", precision = 38, scale = 2)
    private BigDecimal salesAmount = BigDecimal.ZERO;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Column(name = "order_line_count")
    private Integer orderLineCount = 0;

    @Column(name = "shipped_amount", precision = 38, scale = 2)
    private BigDecimal shippedAmount = BigDecimal.ZERO;

    @Column(name = "invoiced_amount", precision = 38, scale = 2)
    private BigDecimal invoicedAmount = BigDecimal.ZERO;

    @Column(name = "customer_count")
    private Integer customerCount = 0;

    @Column(name = "product_count")
    private Integer productCount = 0;

    @Column(name = "avg_order_amount", precision = 38, scale = 2)
    private BigDecimal avgOrderAmount = BigDecimal.ZERO;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
