package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_customer")
public class SalesCustomer extends BaseEntity {

    private String name;

    private String phone;

    private String email;

    private String channel; // 渠道来源

    private Integer isNew; // 0=老客户, 1=新客户

    private LocalDate firstPurchaseDate;

    private Integer repeatPurchaseCount;

    private String level; // VIP/普通
}
