package com.trae.ams.service;

import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.appointment.BookingItemDTO;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.entity.AmsAppointmentItem;
import com.trae.ams.entity.AmsTechnicianInfo;
import com.trae.ams.mapper.AmsAppointmentItemMapper;
import com.trae.ams.mapper.AmsAppointmentMapper;
import com.trae.ams.mapper.AmsTechnicianInfoMapper;
import com.trae.ams.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
public class ComprehensiveAppointmentTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AmsAppointmentMapper appointmentMapper;

    @Autowired
    private AmsAppointmentItemMapper appointmentItemMapper;
    
    @Autowired
    private AmsTechnicianInfoMapper technicianMapper;
    
    @MockBean
    private PaymentService paymentService;

    /**
     * Test Case 1: Normal Appointment Creation
     * Verify: ID returned, Status is PENDING_PAYMENT, Amount is calculated correctly.
     */
    @Test
    @Transactional
    public void testCreateAppointment_Normal() {
        BookingRequest request = new BookingRequest();
        request.setCustomerId(1L); // Assuming user 1 exists
        request.setStoreId(1L);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)); // Tomorrow 10:00
        
        BookingItemDTO item = new BookingItemDTO();
        item.setServiceId(1L); // Assuming service 1 exists (Price e.g. 100)
        request.setItems(Collections.singletonList(item));

        Long appointmentId = appointmentService.createAppointment(request);
        Assertions.assertNotNull(appointmentId);

        AmsAppointment appt = appointmentMapper.selectById(appointmentId);
        Assertions.assertNotNull(appt);
        Assertions.assertEquals(AmsAppointment.STATUS_PENDING_PAYMENT, appt.getStatus());
        Assertions.assertTrue(appt.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Test Case 2: Resource Conflict (Double Booking)
     * Scenario: User A books 10:00-11:00 with Tech T1. User B tries to book same time with Tech T1.
     * Expected: BusinessException "Technician busy".
     */
    @Test
    @Transactional
    public void testCreateAppointment_Conflict() {
        // Get a valid technician
        List<AmsTechnicianInfo> techs = technicianMapper.selectList(1L, null, null);
        Assertions.assertFalse(techs.isEmpty(), "No technicians found for store 1");
        Long validTechId = techs.get(0).getUserId();
        
        LocalDateTime time = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);

        // 1. First Booking
        BookingRequest req1 = new BookingRequest();
        req1.setCustomerId(1L);
        req1.setStoreId(1L);
        req1.setStartTime(time);
        BookingItemDTO item1 = new BookingItemDTO();
        item1.setServiceId(1L); 
        item1.setTechId(validTechId); // Specific Valid Tech
        req1.setItems(Collections.singletonList(item1));
        
        appointmentService.createAppointment(req1);

        // 2. Second Booking (Same Tech, Same Time)
        BookingRequest req2 = new BookingRequest();
        req2.setCustomerId(2L); // Different user
        req2.setStoreId(1L);
        req2.setStartTime(time); // Overlap
        BookingItemDTO item2 = new BookingItemDTO();
        item2.setServiceId(1L);
        item2.setTechId(validTechId); // Same Tech
        req2.setItems(Collections.singletonList(item2));

        Assertions.assertThrows(BusinessException.class, () -> {
            appointmentService.createAppointment(req2);
        });
    }

    /**
     * Test Case 3: Cancel Appointment - Full Refund (>24h)
     */
    @Test
    @Transactional
    public void testCancelAppointment_FullRefund() {
        // Create an appointment far in future (>24h)
        LocalDateTime time = LocalDateTime.now().plusHours(25);
        
        BookingRequest req = new BookingRequest();
        req.setCustomerId(1L);
        req.setStoreId(1L);
        req.setStartTime(time);
        BookingItemDTO item = new BookingItemDTO();
        item.setServiceId(1L);
        req.setItems(Collections.singletonList(item));
        
        Long id = appointmentService.createAppointment(req);
        
        // Simulate Payment (Manually update status to PAID)
        AmsAppointment appt = appointmentMapper.selectById(id);
        appt.setPaymentStatus("PAID");
        appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION); // 5
        appointmentMapper.updateStatus(id, 5);
        appointmentMapper.update(appt); // Update payment status

        // Cancel
        appointmentService.cancelAppointment(id);
        
        // Verify
        AmsAppointment cancelledAppt = appointmentMapper.selectById(id);
        Assertions.assertEquals(AmsAppointment.STATUS_CANCELLED, cancelledAppt.getStatus()); // 3
        // Check Refund Status (Mocked payment service should set it to COMPLETED)
        Assertions.assertEquals(AmsAppointment.REFUND_STATUS_COMPLETED, cancelledAppt.getRefundStatus());
    }

    /**
     * Test Case 4: Cancel Appointment - Violation (<12h)
     */
    @Test
    @Transactional
    public void testCancelAppointment_Violation() {
        // Create an appointment soon (<12h)
        LocalDateTime time = LocalDateTime.now().plusHours(10);
        
        BookingRequest req = new BookingRequest();
        req.setCustomerId(1L);
        req.setStoreId(1L);
        req.setStartTime(time);
        BookingItemDTO item = new BookingItemDTO();
        item.setServiceId(1L);
        req.setItems(Collections.singletonList(item));
        
        Long id = appointmentService.createAppointment(req);
        
        // Simulate Payment
        AmsAppointment appt = appointmentMapper.selectById(id);
        appt.setPaymentStatus("PAID");
        appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
        appointmentMapper.update(appt);

        // Cancel
        appointmentService.cancelAppointment(id);
        
        // Verify
        AmsAppointment cancelledAppt = appointmentMapper.selectById(id);
        Assertions.assertEquals(AmsAppointment.STATUS_VIOLATION, cancelledAppt.getStatus()); // 4
    }
}
