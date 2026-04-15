package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_order")
public class SalesOrder extends BaseEntity {

    private String orderNo;

    private LocalDate orderDate;

    private String customerName;

    private String salesperson;

    private String source; // 线上/线下/渠道

    private BigDecimal amount;

    private Integer status; // 0=pending, 1=confirmed, 2=shipped, 3=completed
}
