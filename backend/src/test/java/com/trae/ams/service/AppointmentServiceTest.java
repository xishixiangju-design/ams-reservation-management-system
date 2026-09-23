package com.trae.ams.service;

import cn.hutool.core.collection.CollUtil;
import com.trae.ams.dto.appointment.BatchBookingRequest;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.mapper.AmsAppointmentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AmsAppointmentMapper appointmentMapper;

    @Test
    @Transactional
    public void testBatchCreateAndPay() {
        // Prepare request
        BatchBookingRequest req = new BatchBookingRequest();
        req.setCustomerId(5L); // Customer 001
        req.setServiceIds(Arrays.asList(1L, 2L)); // Service 1, 2 exist in initial data
        // Use future times to avoid conflicts with past/current
        LocalDateTime time1 = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime time2 = LocalDateTime.now().plusDays(10).withHour(14).withMinute(0).withSecond(0).withNano(0);
        
        req.setStartTimes(Arrays.asList(time1, time2));
        req.setStoreId(1L);
        req.setRemark("Batch Test");

        // Execute Batch Create
        List<Long> ids = appointmentService.batchCreateAppointment(req);
        
        // Assertions
        Assertions.assertNotNull(ids);
        Assertions.assertEquals(4, ids.size(), "Should create 2 services * 2 times = 4 appointments");

        // Verify Status (Pending Payment = 1)
        for (Long id : ids) {
            AmsAppointment appt = appointmentMapper.selectById(id);
            Assertions.assertNotNull(appt);
            Assertions.assertEquals(AmsAppointment.STATUS_PENDING_PAYMENT, appt.getStatus(), "Initial status should be PENDING_PAYMENT");
        }

        // Execute Batch Pay
        appointmentService.batchPay(ids);

        // Verify Status (Pending Consumption = 5)
        for (Long id : ids) {
            AmsAppointment appt = appointmentMapper.selectById(id);
            Assertions.assertEquals(AmsAppointment.STATUS_PENDING_CONSUMPTION, appt.getStatus(), "Status after pay should be PENDING_CONSUMPTION");
        }
    }
}
