package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dim_customer")
public class DimCustomer extends BaseEntity {

    @Column(name = "customer_code", unique = true, nullable = false, length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_short_name", length = 50)
    private String customerShortName;

    @Column(name = "customer_category_code", length = 20)
    private String customerCategoryCode;

    @Column(name = "customer_category_desc", length = 50)
    private String customerCategoryDesc;

    @Column(name = "sales_region_code", length = 50)
    private String salesRegionCode;

    @Column(name = "sales_region_desc", length = 50)
    private String salesRegionDesc;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "payment_terms_desc", length = 100)
    private String paymentTermsDesc;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
