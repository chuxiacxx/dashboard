package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_monthly_summary",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year", "month", "customer_code"}))
public class CustomerMonthlySummary extends BaseEntity {

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "customer_code", nullable = false, length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "sales_amount", precision = 38, scale = 2)
    private BigDecimal salesAmount = BigDecimal.ZERO;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Column(name = "product_count")
    private Integer productCount = 0;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
