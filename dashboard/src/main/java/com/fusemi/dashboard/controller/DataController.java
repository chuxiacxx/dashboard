package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.service.DataService;
import com.fusemi.dashboard.vo.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 业务数据 Controller
 * 提供聚合统计数据供仪表盘使用
 */
@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<SalesMonthlyVO>> getSalesList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return Result.ok(dataService.getSalesMonthlyData(startDate, endDate));
        }
        return Result.ok(dataService.getSalesMonthlyData(year));
    }

    @GetMapping("/shipment")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<SalesShipmentMonthlyVO>> getShipmentList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return Result.ok(dataService.getShipmentMonthlyData(startDate, endDate));
        }
        return Result.ok(dataService.getShipmentMonthlyData(year));
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<SalesProductVO>> getProductRanking(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return Result.ok(dataService.getProductRanking(startDate, endDate, limit));
        }
        return Result.ok(dataService.getProductRanking(year, month, limit));
    }

    @GetMapping("/salespersons")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_SALES')")
    public Result<List<SalesPersonVO>> getSalespersonRanking(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return Result.ok(dataService.getSalespersonRanking(startDate, endDate));
        }
        return Result.ok(dataService.getSalespersonRanking(year, month));
    }

    @GetMapping("/regions")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_SALES')")
    public Result<List<SalesRegionVO>> getRegionStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return Result.ok(dataService.getRegionStats(startDate, endDate));
        }
        return Result.ok(dataService.getRegionStats(year, month));
    }

    @GetMapping("/years")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<Integer>> getAvailableYears() {
        return Result.ok(dataService.getAvailableYears());
    }

    @GetMapping("/salespersons/list")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<String>> getSalespersonList() {
        return Result.ok(dataService.getSalespersonList());
    }

    @GetMapping("/regions/list")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<String>> getRegionList() {
        return Result.ok(dataService.getRegionList());
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<OrderTrackingVO>> getRecentOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(dataService.getRecentOrders(startDate, endDate));
    }

    @GetMapping("/orders/overdue")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_USER', 'R_SALES')")
    public Result<List<OrderTrackingVO>> getOverdueOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(dataService.getOverdueOrders(startDate, endDate));
    }
}
