package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dim_salesperson")
public class DimSalesperson extends BaseEntity {

    @Column(name = "salesperson_code", unique = true, nullable = false, length = 50)
    private String salespersonCode;

    @Column(name = "salesperson_name", length = 50)
    private String salespersonName;

    @Column(name = "sales_org_code", length = 50)
    private String salesOrgCode;

    @Column(name = "sales_org_desc", length = 50)
    private String salesOrgDesc;

    @Column(name = "sales_region_code", length = 50)
    private String salesRegionCode;

    @Column(name = "sales_region_desc", length = 50)
    private String salesRegionDesc;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
