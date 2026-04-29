package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.entity.SalesTarget;
import com.fusemi.dashboard.service.SalesTargetService;
import com.fusemi.dashboard.vo.SalesMonthlyVO;
import com.fusemi.dashboard.vo.SalesPersonVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 销售目标配置 Controller
 * 仅超级管理员可操作
 */
@RestController
@RequestMapping("/api/sales-target")
@RequiredArgsConstructor
public class SalesTargetController {

    private final SalesTargetService salesTargetService;

    /**
     * 获取目标列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN')")
    public Result<List<SalesTarget>> getTargets(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return Result.ok(salesTargetService.getAllTargets(year, month));
    }

    /**
     * 创建目标
     */
    @PostMapping
    @PreAuthorize("hasRole('R_SUPER')")
    public Result<SalesTarget> createTarget(@RequestBody SalesTarget target) {
        return Result.ok(salesTargetService.createTarget(target));
    }

    /**
     * 更新目标
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('R_SUPER')")
    public Result<SalesTarget> updateTarget(@PathVariable Long id, @RequestBody SalesTarget target) {
        return Result.ok(salesTargetService.updateTarget(id, target));
    }

    /**
     * 删除目标
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('R_SUPER')")
    public Result<Void> deleteTarget(@PathVariable Long id) {
        salesTargetService.deleteTarget(id);
        return Result.ok();
    }

    /**
     * 批量创建/更新目标
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('R_SUPER')")
    public Result<List<SalesTarget>> batchCreateTargets(@RequestBody List<SalesTarget> targets) {
        return Result.ok(salesTargetService.batchCreateTargets(targets));
    }

    /**
     * 获取销售员业绩排行
     */
    @GetMapping("/ranking")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN', 'R_SALES')")
    public Result<List<SalesPersonVO>> getSalespersonRanking(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return Result.ok(salesTargetService.getSalespersonRanking(year, month));
    }

    /**
     * 获取月度目标达成情况
     */
    @GetMapping("/monthly-achievement")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN')")
    public Result<List<SalesMonthlyVO>> getMonthlyAchievement(
            @RequestParam Integer year) {
        return Result.ok(salesTargetService.getMonthlyAchievement(year));
    }
}
