package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_order_line",
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_no", "order_line_no"}))
public class SalesOrderLine extends BaseEntity {

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "order_line_no", nullable = false, length = 20)
    private String orderLineNo;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "product_full_name", length = 200)
    private String productFullName;

    @Column(name = "spec_model", length = 100)
    private String specModel;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "order_quantity", precision = 38, scale = 2)
    private BigDecimal orderQuantity;

    @Column(name = "unit_price", precision = 38, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "unit_price_no_tax", precision = 38, scale = 4)
    private BigDecimal unitPriceNoTax;

    @Column(name = "order_amount_no_tax", precision = 38, scale = 2)
    private BigDecimal orderAmountNoTax;

    @Column(name = "order_amount_tax", precision = 38, scale = 2)
    private BigDecimal orderAmountTax;

    @Column(name = "order_tax", precision = 38, scale = 2)
    private BigDecimal orderTax;

    @Column(name = "shipped_quantity", precision = 38, scale = 2)
    private BigDecimal shippedQuantity;

    @Column(name = "shipped_amount_no_tax", precision = 38, scale = 2)
    private BigDecimal shippedAmountNoTax;

    @Column(name = "shipped_amount_tax", precision = 38, scale = 2)
    private BigDecimal shippedAmountTax;

    @Column(name = "unshipped_quantity", precision = 38, scale = 2)
    private BigDecimal unshippedQuantity;

    @Column(name = "unshipped_amount_no_tax", precision = 38, scale = 2)
    private BigDecimal unshippedAmountNoTax;

    @Column(name = "unshipped_amount_tax", precision = 38, scale = 2)
    private BigDecimal unshippedAmountTax;

    @Column(name = "invoiced_quantity", precision = 38, scale = 2)
    private BigDecimal invoicedQuantity;

    @Column(name = "invoiced_amount_no_tax", precision = 38, scale = 2)
    private BigDecimal invoicedAmountNoTax;

    @Column(name = "invoiced_amount_tax", precision = 38, scale = 2)
    private BigDecimal invoicedAmountTax;

    @Column(name = "uninvoiced_quantity", precision = 38, scale = 2)
    private BigDecimal uninvoicedQuantity;

    @Column(name = "uninvoiced_amount_no_tax", precision = 38, scale = 2)
    private BigDecimal uninvoicedAmountNoTax;

    @Column(name = "uninvoiced_amount_tax", precision = 38, scale = 2)
    private BigDecimal uninvoicedAmountTax;

    @Column(name = "original_quantity", precision = 38, scale = 2)
    private BigDecimal originalQuantity;

    @Column(name = "original_order_no", length = 50)
    private String originalOrderNo;

    @Column(name = "reference_doc", length = 50)
    private String referenceDoc;

    @Column(name = "reference_item", length = 50)
    private String referenceItem;

    @Column(name = "over_delivery_quantity", precision = 38, scale = 2)
    private BigDecimal overDeliveryQuantity;

    @Column(name = "over_delivery_amount", precision = 38, scale = 2)
    private BigDecimal overDeliveryAmount;

    @Column(name = "is_over_delivery", length = 10)
    private String isOverDelivery;

    @Column(name = "line_remark", length = 500)
    private String lineRemark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
