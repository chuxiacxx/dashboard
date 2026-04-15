package com.fusemi.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sales_shipment")
public class SalesShipment extends BaseEntity {

    private String shipmentNo;

    private String orderNo;

    private LocalDate shipmentDate;

    private String carrier;

    private String status; // pending/in_transit/delivered

    private BigDecimal amount;

    private String receiverAddress;

    private String receiverPhone;
}
