package com.fusemi.dashboard.service.parser;

import com.fusemi.dashboard.entity.SalesData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class SalesDataRowMapper {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    };

    public SalesData map(Map<String, String> row) {
        SalesData data = new SalesData();

        data.setSaleDate(parseDate(getValue(row, "saleDate", "日期", "销售日期")));
        data.setRegion(getValue(row, "region", "地区", "区域"));
        data.setSalesperson(getValue(row, "salesperson", "销售员", "销售人员"));
        data.setProductName(getValue(row, "productName", "产品名称", "产品", "商品"));
        data.setAmount(parseBigDecimal(getValue(row, "amount", "金额", "销售额")));
        data.setQuantity(parseInt(getValue(row, "quantity", "数量", "销量")));
        data.setUnitPrice(parseBigDecimal(getValue(row, "unitPrice", "单价", "单位价格")));
        data.setCustomerName(getValue(row, "customerName", "客户名称", "客户", "客户名"));
        data.setOrderNo(getValue(row, "orderNo", "订单号", "订单编号"));
        data.setStatus(getValue(row, "status", "状态"));

        return data;
    }

    /**
     * 尝试多个可能的列名获取值
     */
    private String getValue(Map<String, String> row, String... possibleKeys) {
        for (String key : possibleKeys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        return "";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
