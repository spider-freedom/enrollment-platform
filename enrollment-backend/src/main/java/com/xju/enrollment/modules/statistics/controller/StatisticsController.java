package com.xju.enrollment.modules.statistics.controller;

import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.modules.statistics.dto.CollegeStatVO;
import com.xju.enrollment.modules.statistics.dto.DashboardVO;
import com.xju.enrollment.modules.statistics.dto.TrendVO;
import com.xju.enrollment.modules.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardVO> getDashboard() {
        DashboardVO dashboard = statisticsService.getDashboard();
        return ApiResponse.ok(dashboard);
    }

    @GetMapping("/trend")
    public ApiResponse<TrendVO> getMonthlyTrend() {
        TrendVO trend = statisticsService.getMonthlyTrend();
        return ApiResponse.ok(trend);
    }

    @GetMapping("/college")
    public ApiResponse<List<CollegeStatVO>> getCollegeDistribution() {
        List<CollegeStatVO> distribution = statisticsService.getCollegeDistribution();
        return ApiResponse.ok(distribution);
    }

    @GetMapping("/rating")
    public ApiResponse<Map<Integer, Long>> getRatingDistribution() {
        Map<Integer, Long> distribution = statisticsService.getRatingDistribution();
        return ApiResponse.ok(distribution);
    }
}
