package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_deal")
public class SalesDeal extends BaseEntity {

    private LocalDate dealDate;

    private String productName;

    private BigDecimal amount;

    private Integer quantity;

    private BigDecimal conversionRate;

    private String stage; // 意向/洽谈/签约
}
