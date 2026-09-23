package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import com.trae.ams.dto.dashboard.DashboardSummaryDTO;
import com.trae.ams.dto.dashboard.RoomStatusDTO;
import com.trae.ams.dto.dashboard.TechStatusDTO;
import com.trae.ams.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired private DashboardService dashboardService;

    @GetMapping("/summary")
    public Result<DashboardSummaryDTO> getSummary() {
        return Result.success(dashboardService.getSummary());
    }

    @GetMapping("/room-status")
    public Result<List<RoomStatusDTO>> getRoomStatus() {
        return Result.success(dashboardService.getRoomStatus());
    }

    @GetMapping("/tech-status")
    public Result<List<TechStatusDTO>> getTechStatus() {
        return Result.success(dashboardService.getTechStatus());
    }

    @GetMapping("/calendar-stats")
    public Result<List<java.util.Map<String, Object>>> getCalendarStats(String startDate, String endDate) {
        return Result.success(dashboardService.getCalendarStats(startDate, endDate));
    }
}
