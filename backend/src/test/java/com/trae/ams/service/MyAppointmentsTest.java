package com.trae.ams.service;

import com.github.pagehelper.PageInfo;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.entity.AmsAppointmentItem;
import com.trae.ams.entity.AmsService;
import com.trae.ams.mapper.AmsAppointmentItemMapper;
import com.trae.ams.mapper.AmsAppointmentMapper;
import com.trae.ams.mapper.AmsServiceMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Transactional
public class MyAppointmentsTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AmsAppointmentMapper appointmentMapper;
    
    @Autowired
    private AmsAppointmentItemMapper itemMapper;
    
    @Autowired
    private AmsServiceMapper serviceMapper;

    @Test
    public void testListMyAppointments() {
        Long userId = 100L;
        Long storeId = 1L;
        
        // Ensure service exists
        AmsService service = new AmsService();
        service.setStoreId(storeId);
        service.setName("Test Service");
        service.setPrice(new BigDecimal("100"));
        service.setDuration(60);
        service.setStatus(1);
        service.setCreateTime(LocalDateTime.now());
        serviceMapper.insert(service);
        Long serviceId = service.getId();
        
        // Create appointments
        // 1. Pending Consumption, 2 days ago
        createAppt(userId, storeId, LocalDateTime.now().minusDays(2), 5, serviceId);
        // 2. Completed, 10 days ago
        createAppt(userId, storeId, LocalDateTime.now().minusDays(10), 2, serviceId);
        // 3. Pending Consumption (Future), tomorrow
        createAppt(userId, storeId, LocalDateTime.now().plusDays(1), 5, serviceId);
        
        // 1. Test All (No Date Filter)
        PageInfo<AmsAppointment> page = appointmentService.listMyAppointments(userId, 1, 10, null, null, null);
        Assertions.assertEquals(3, page.getList().size());
        
        // 2. Test Date Range (Last 7 days: now-7d to now)
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        page = appointmentService.listMyAppointments(userId, 1, 10, start, end, null);
        // Should find the one 2 days ago only.
        // The one 10 days ago is before start.
        // The one tomorrow is after end.
        Assertions.assertEquals(1, page.getList().size());
        Assertions.assertEquals(5, page.getList().get(0).getStatus()); // Pending Consumption one
        
        // 3. Test Status (Completed)
        page = appointmentService.listMyAppointments(userId, 1, 10, null, null, 2);
        Assertions.assertEquals(1, page.getList().size());
        Assertions.assertEquals(2, page.getList().get(0).getStatus());
        
        // 4. Test Item Enrichment
        AmsAppointment appt = page.getList().get(0);
        Assertions.assertNotNull(appt.getItems());
        Assertions.assertFalse(appt.getItems().isEmpty());
        Assertions.assertEquals("Test Service", appt.getItems().get(0).getService().getName());
    }
    
    private void createAppt(Long userId, Long storeId, LocalDateTime startTime, Integer status, Long serviceId) {
        AmsAppointment appt = new AmsAppointment();
        appt.setStoreId(storeId);
        appt.setCustomerId(userId);
        appt.setStartTime(startTime);
        appt.setEndTime(startTime.plusHours(1));
        appt.setStatus(status);
        appt.setPeopleCount(1);
        appt.setTotalAmount(new BigDecimal("100"));
        appt.setCreateTime(LocalDateTime.now());
        appointmentMapper.insert(appt);
        
        AmsAppointmentItem item = new AmsAppointmentItem();
        item.setApptId(appt.getId());
        item.setServiceId(serviceId);
        item.setPrice(new BigDecimal("100"));
        itemMapper.batchInsert(Collections.singletonList(item));
    }
}
