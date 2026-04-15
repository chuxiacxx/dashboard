package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.service.DashboardService;
import com.fusemi.dashboard.vo.DashboardSummaryVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("isAuthenticated()")
    public Result<DashboardSummaryVO> getSummary() {
        return Result.ok(dashboardService.getSummary());
    }
}
