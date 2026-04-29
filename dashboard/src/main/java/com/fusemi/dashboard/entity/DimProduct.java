package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dim_product")
public class DimProduct extends BaseEntity {

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "product_full_name", length = 200)
    private String productFullName;

    @Column(name = "spec_model", length = 100)
    private String specModel;

    @Column(name = "product_line_code", length = 50)
    private String productLineCode;

    @Column(name = "product_line_desc", length = 50)
    private String productLineDesc;

    @Column(name = "product_group_code", length = 20)
    private String productGroupCode;

    @Column(name = "product_group_desc", length = 50)
    private String productGroupDesc;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
