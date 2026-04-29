package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 销售目标配置实体
 * 超级管理员可配置各维度销售目标
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_target",
    uniqueConstraints = @UniqueConstraint(columnNames = {"year", "month", "salesperson", "region"}, name = "uk_target"))
public class SalesTarget extends BaseEntity {

    /** 年份 */
    @Column(nullable = false)
    private Integer year;

    /** 月份 (1-12) */
    @Column(nullable = false)
    private Integer month;

    /** 销售员姓名（为空表示全部） */
    @Column(length = 50)
    private String salesperson;

    /** 销售地区（为空表示全部） */
    @Column(length = 50)
    private String region;

    /** 销售目标金额 */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal targetAmount;

    /** 回款目标金额 */
    @Column(precision = 18, scale = 2)
    private BigDecimal collectionTargetAmount;

    /** 订单数量目标 */
    private Integer orderCountTarget;

    /** 备注 */
    @Column(length = 200)
    private String remark;

    /**
     * 获取年月字符串，如 "2024-01"
     */
    public String getYearMonth() {
        return String.format("%d-%02d", year, month);
    }
}
