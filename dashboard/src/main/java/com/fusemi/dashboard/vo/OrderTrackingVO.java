package com.fusemi.dashboard.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单跟踪 VO
 */
@Data
public class OrderTrackingVO {
    private String orderNo;
    private String customerName;
    private LocalDate createDate;
    private String status;
    private BigDecimal amount;
    private Integer overdueDays;
}
