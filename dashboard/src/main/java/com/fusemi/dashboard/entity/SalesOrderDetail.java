package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销售订单明细实体
 * 对应 Excel 导入的完整销售订单数据（77列）
 * 唯一键：orderNo + orderLineNo
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_order_detail",
    uniqueConstraints = @UniqueConstraint(columnNames = {"order_no", "order_line_no"}, name = "uk_order_line"))
public class SalesOrderDetail extends BaseEntity {

    // ========== 订单基本信息 ==========
    /** 订单创建时间 */
    private LocalDate orderCreateDate;

    /** 订单编号 */
    @Column(name = "order_no", length = 50)
    private String orderNo;

    /** 订单类型描述 */
    @Column(length = 50)
    private String orderTypeDesc;

    /** 销售员姓名 */
    @Column(length = 50)
    private String salespersonName;

    /** 客户名称 */
    @Column(length = 100)
    private String customerName;

    /** 销售合同号 */
    @Column(length = 50)
    private String contractNo;

    /** 客户PO号 */
    @Column(length = 50)
    private String customerPoNo;

    // ========== 产品信息 ==========
    /** 销货名称 */
    @Column(length = 100)
    private String productName;

    /** 规格型号 */
    @Column(length = 100)
    private String specModel;

    /** 产品编号 */
    @Column(length = 50)
    private String productCode;

    /** 产品名称（完整） */
    @Column(length = 200)
    private String productFullName;

    /** 产品组描述 */
    @Column(length = 50)
    private String productGroupDesc;

    /** 产品线描述 */
    @Column(length = 50)
    private String productLineDesc;

    /** 所属产品线编码 */
    @Column(length = 50)
    private String productLineCode;

    // ========== 数量与金额 ==========
    /** 订单数量 */
    private BigDecimal orderQuantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 订单金额(含税) */
    private BigDecimal orderAmountTax;

    /** 订单金额(不含税) */
    private BigDecimal orderAmountNoTax;

    /** 订单税额 */
    private BigDecimal orderTax;

    /** 不含税单价 */
    private BigDecimal unitPriceNoTax;

    /** 原订单数量 */
    private BigDecimal originalQuantity;

    /** 单位 */
    @Column(length = 20)
    private String unit;

    // ========== 发货信息 ==========
    /** 预计交货日期 */
    private LocalDate estimatedDeliveryDate;

    /** 已发货数量 */
    private BigDecimal shippedQuantity;

    /** 未发货数量 */
    private BigDecimal unshippedQuantity;

    /** 已发货金额（含税） */
    private BigDecimal shippedAmountTax;

    /** 已发货金额（不含税） */
    private BigDecimal shippedAmountNoTax;

    /** 未发货金额(含税) */
    private BigDecimal unshippedAmountTax;

    /** 未发货金额（不含税） */
    private BigDecimal unshippedAmountNoTax;

    // ========== 开票信息 ==========
    /** 已开票数量 */
    private BigDecimal invoicedQuantity;

    /** 已开票金额(含税) */
    private BigDecimal invoicedAmountTax;

    /** 已开票金额(不含税) */
    private BigDecimal invoicedAmountNoTax;

    /** 未开票数量 */
    private BigDecimal uninvoicedQuantity;

    /** 未开票金额(含税) */
    private BigDecimal uninvoicedAmountTax;

    /** 未开票金额(不含税) */
    private BigDecimal uninvoicedAmountNoTax;

    // ========== 客户信息 ==========
    /** 客户编码 */
    @Column(length = 50)
    private String customerCode;

    /** 客户简称 */
    @Column(length = 50)
    private String customerShortName;

    /** 客户分类描述 */
    @Column(length = 50)
    private String customerCategoryDesc;

    /** 客户分类编码 */
    @Column(length = 20)
    private String customerCategoryCode;

    /** 客户物料 */
    @Column(length = 100)
    private String customerMaterial;

    // ========== 销售组织信息 ==========
    /** 销售地区描述 */
    @Column(length = 50)
    private String salesRegionDesc;

    /** 销售地区编码 */
    @Column(length = 50)
    private String salesRegionCode;

    /** 所属细分市场描述 */
    @Column(length = 100)
    private String marketSegmentDesc;

    /** 所属细分市场编码 */
    @Column(length = 50)
    private String marketSegmentCode;

    /** 销售组织描述 */
    @Column(length = 50)
    private String salesOrgDesc;

    /** 销售组织编码 */
    @Column(length = 50)
    private String salesOrgCode;

    /** 分销渠道描述 */
    @Column(length = 50)
    private String distributionChannelDesc;

    /** 分销渠道编码 */
    @Column(length = 20)
    private String distributionChannelCode;

    /** 产品组编码 */
    @Column(length = 20)
    private String productGroupCode;

    // ========== 订单状态 ==========
    /** 拒绝原因编码 */
    @Column(length = 10)
    private String rejectReasonCode;

    /** 拒绝原因描述 */
    @Column(length = 100)
    private String rejectReasonDesc;

    /** 订单状态：0=正常, 1=有拒绝原因 */
    private Integer orderStatus;

    /** 是否过量交货 */
    @Column(length = 10)
    private String isOverDelivery;

    /** 过量交货数量 */
    private BigDecimal overDeliveryQuantity;

    /** 过量交货金额（含税） */
    private BigDecimal overDeliveryAmount;

    // ========== 其他信息 ==========
    /** 原T+订单号 */
    @Column(length = 50)
    private String originalOrderNo;

    /** 销售订单行号 */
    @Column(name = "order_line_no", length = 20)
    private String orderLineNo;

    /** 订单类型编码 */
    @Column(length = 20)
    private String orderTypeCode;

    /** 订单原因编码 */
    @Column(length = 20)
    private String orderReasonCode;

    /** 订单原因描述 */
    @Column(length = 100)
    private String orderReasonDesc;

    /** 创建人 */
    @Column(length = 50)
    private String creator;

    /** 付款条件 */
    @Column(length = 50)
    private String paymentTerms;

    /** 付款条件描述 */
    @Column(length = 100)
    private String paymentTermsDesc;

    /** 币种 */
    @Column(length = 10)
    private String currency;

    /** 汇率 */
    private BigDecimal exchangeRate;

    /** 销售员编码 */
    @Column(length = 50)
    private String salespersonCode;

    /** 销售助理 */
    @Column(length = 50)
    private String salesAssistant;

    /** 销售助理姓名 */
    @Column(length = 50)
    private String salesAssistantName;

    /** FAE */
    @Column(length = 50)
    private String fae;

    /** FAE姓名 */
    @Column(length = 50)
    private String faeName;

    /** 行备注 */
    @Column(length = 500)
    private String lineRemark;

    /** 抬头备注 */
    @Column(length = 500)
    private String headerRemark;

    /** 参考凭证 */
    @Column(length = 50)
    private String referenceDoc;

    /** 参考项目 */
    @Column(length = 50)
    private String referenceItem;

    /** 参考凭证币种 */
    @Column(length = 10)
    private String referenceCurrency;

    /** 参考凭证汇率 */
    private BigDecimal referenceExchangeRate;

    /** 供应链建议交期 */
    private LocalDate supplyChainDeliveryDate;

    /** 客户po日期 */
    private LocalDate customerPoDate;
}
