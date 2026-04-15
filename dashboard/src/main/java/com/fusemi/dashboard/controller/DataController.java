package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.service.DataService;
import com.fusemi.dashboard.vo.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 业务数据 Controller
 */
@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/sales")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getSalesList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(dataService.getSalesPage(current, size, startDate, endDate));
    }

    @GetMapping("/shipment")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getShipmentList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(dataService.getShipmentPage(current, size, startDate, endDate));
    }

    @GetMapping("/order")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getOrderList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(dataService.getOrderPage(current, size, startDate, endDate));
    }

    @GetMapping("/invoice")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getInvoiceList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(dataService.getInvoicePage(current, size, startDate, endDate));
    }

    @GetMapping("/deal")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getDealList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(dataService.getDealPage(current, size, startDate, endDate));
    }

    @GetMapping("/customer")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getCustomerList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer isNew) {
        return Result.ok(dataService.getCustomerPage(current, size, channel, isNew));
    }
}
