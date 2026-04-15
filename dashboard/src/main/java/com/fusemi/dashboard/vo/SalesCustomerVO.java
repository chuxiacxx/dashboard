package com.fusemi.dashboard.vo;

import lombok.Data;
import java.time.LocalDate;

/**
 * 客户数据 VO
 */
@Data
public class SalesCustomerVO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String channel;
    private Integer isNew;
    private LocalDate firstPurchaseDate;
    private Integer repeatPurchaseCount;
    private String level;
}
