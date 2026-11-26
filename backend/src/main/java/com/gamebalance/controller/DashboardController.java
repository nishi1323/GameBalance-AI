package com.gamebalance.controller;

import com.gamebalance.dto.DashboardOverviewResponse;
import com.gamebalance.security.AuthUserPrincipal;
import com.gamebalance.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getOverview(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getOverview(principal.getId()));
    }
}
