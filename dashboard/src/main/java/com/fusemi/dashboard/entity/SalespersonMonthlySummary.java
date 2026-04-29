package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "salesperson_monthly_summary",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year", "month", "salesperson_code"}))
public class SalespersonMonthlySummary extends BaseEntity {

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "salesperson_code", nullable = false, length = 50)
    private String salespersonCode;

    @Column(name = "salesperson_name", length = 50)
    private String salespersonName;

    @Column(name = "sales_region_desc", length = 50)
    private String salesRegionDesc;

    @Column(name = "sales_amount", precision = 38, scale = 2)
    private BigDecimal salesAmount = BigDecimal.ZERO;

    @Column(name = "shipped_amount", precision = 38, scale = 2)
    private BigDecimal shippedAmount = BigDecimal.ZERO;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Column(name = "target_amount", precision = 38, scale = 2)
    private BigDecimal targetAmount = BigDecimal.ZERO;

    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
