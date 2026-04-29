package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_order_master")
public class SalesOrderMaster extends BaseEntity {

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @Column(name = "order_type_code", length = 20)
    private String orderTypeCode;

    @Column(name = "order_type_desc", length = 50)
    private String orderTypeDesc;

    @Column(name = "order_status")
    private Integer orderStatus = 0;

    @Column(name = "order_create_date")
    private LocalDate orderCreateDate;

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_po_no", length = 50)
    private String customerPoNo;

    @Column(name = "customer_po_date")
    private LocalDate customerPoDate;

    @Column(name = "contract_no", length = 50)
    private String contractNo;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "exchange_rate", precision = 38, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "reference_currency", length = 10)
    private String referenceCurrency;

    @Column(name = "reference_exchange_rate", precision = 38, scale = 6)
    private BigDecimal referenceExchangeRate;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Column(name = "payment_terms_desc", length = 100)
    private String paymentTermsDesc;

    @Column(name = "sales_org_code", length = 50)
    private String salesOrgCode;

    @Column(name = "sales_org_desc", length = 50)
    private String salesOrgDesc;

    @Column(name = "sales_region_code", length = 50)
    private String salesRegionCode;

    @Column(name = "sales_region_desc", length = 50)
    private String salesRegionDesc;

    @Column(name = "distribution_channel_code", length = 20)
    private String distributionChannelCode;

    @Column(name = "distribution_channel_desc", length = 50)
    private String distributionChannelDesc;

    @Column(name = "market_segment_code", length = 50)
    private String marketSegmentCode;

    @Column(name = "market_segment_desc", length = 100)
    private String marketSegmentDesc;

    @Column(name = "salesperson_code", length = 50)
    private String salespersonCode;

    @Column(name = "salesperson_name", length = 50)
    private String salespersonName;

    @Column(name = "sales_assistant", length = 50)
    private String salesAssistant;

    @Column(name = "sales_assistant_name", length = 50)
    private String salesAssistantName;

    @Column(name = "fae", length = 50)
    private String fae;

    @Column(name = "fae_name", length = 50)
    private String faeName;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "supply_chain_delivery_date")
    private LocalDate supplyChainDeliveryDate;

    @Column(name = "header_remark", length = 500)
    private String headerRemark;

    @Column(name = "reject_reason_code", length = 10)
    private String rejectReasonCode;

    @Column(name = "reject_reason_desc", length = 100)
    private String rejectReasonDesc;

    @Column(name = "creator", length = 50)
    private String creator;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
