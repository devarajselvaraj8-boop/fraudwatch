package com.deva.fraudwatch.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deva.fraudwatch.dto.DashboardResponse;
import com.deva.fraudwatch.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/rules")
    public Map<String, Long> getRuleStatistics() {
        return dashboardService.getRuleStatistics();
    }
}
