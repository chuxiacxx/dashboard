package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 发货数据 VO
 */
@Data
public class SalesShipmentVO {
    private Long id;
    private String shipmentNo;
    private String orderNo;
    private LocalDate shipmentDate;
    private String carrier;
    private String status;
    private BigDecimal amount;
    private String receiverAddress;
    private String receiverPhone;
}
