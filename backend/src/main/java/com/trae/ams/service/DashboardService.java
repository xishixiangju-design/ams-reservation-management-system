package com.trae.ams.service;

import com.trae.ams.dto.dashboard.DashboardSummaryDTO;
import com.trae.ams.dto.dashboard.RoomStatusDTO;
import com.trae.ams.dto.dashboard.TechStatusDTO;
import java.util.List;

public interface DashboardService {
    DashboardSummaryDTO getSummary();
    List<RoomStatusDTO> getRoomStatus();
    List<TechStatusDTO> getTechStatus();
    List<java.util.Map<String, Object>> getCalendarStats(String startDate, String endDate);
}
