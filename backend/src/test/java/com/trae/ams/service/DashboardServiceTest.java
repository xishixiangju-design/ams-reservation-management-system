package com.trae.ams.service;

import com.trae.ams.dto.dashboard.DashboardSummaryDTO;
import com.trae.ams.dto.dashboard.RoomStatusDTO;
import com.trae.ams.dto.dashboard.TechStatusDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    public void testGetSummary() {
        DashboardSummaryDTO summary = dashboardService.getSummary();
        assertNotNull(summary);
        System.out.println("Summary: Appointments=" + summary.getTodayAppointments() + 
                           ", Revenue=" + summary.getTodayRevenue() +
                           ", Waitlist=" + summary.getWaitlistCount() +
                           ", Violations=" + summary.getViolationCount());
    }

    @Test
    public void testGetRoomStatus() {
        List<RoomStatusDTO> rooms = dashboardService.getRoomStatus();
        assertNotNull(rooms);
        System.out.println("Room Status Count: " + rooms.size());
        rooms.forEach(r -> System.out.println("Room: " + r.getName() + " Status: " + r.getStatus()));
    }

    @Test
    public void testGetTechStatus() {
        List<TechStatusDTO> techs = dashboardService.getTechStatus();
        assertNotNull(techs);
        System.out.println("Tech Status Count: " + techs.size());
        techs.forEach(t -> System.out.println("Tech: " + t.getName() + " Status: " + t.getStatus()));
    }
}
