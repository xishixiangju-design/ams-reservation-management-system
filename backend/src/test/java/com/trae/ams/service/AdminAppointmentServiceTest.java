package com.trae.ams.service;

import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.mapper.AmsAppointmentItemMapper;
import com.trae.ams.mapper.AmsAppointmentMapper;
import com.trae.ams.mapper.AmsTransactionMapper;
import com.trae.ams.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminAppointmentServiceTest {

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Mock
    private AmsAppointmentMapper appointmentMapper;
    
    @Mock
    private AmsAppointmentItemMapper appointmentItemMapper;

    @Mock
    private AmsTransactionMapper transactionMapper;
    
    @Mock
    private MemberService memberService;

    private MockedStatic<UserContext> userContextMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userContextMock = mockStatic(UserContext.class);
    }

    @AfterEach
    void tearDown() {
        userContextMock.close();
    }

    @Test
    void testMarkAsPaid_Success() {
        Long apptId = 1L;
        AmsAppointment appt = new AmsAppointment();
        appt.setId(apptId);
        appt.setStatus(AmsAppointment.STATUS_PENDING_PAYMENT);
        appt.setPaymentStatus("UNPAID");
        appt.setTotalAmount(new BigDecimal("100.00"));
        appt.setStoreId(1L);

        when(appointmentMapper.selectById(apptId)).thenReturn(appt);

        appointmentService.markAsPaid(apptId, "ADMIN");

        verify(transactionMapper, times(1)).insert(any());
        verify(appointmentMapper, times(1)).update(any());
        assertEquals("PAID", appt.getPaymentStatus());
        assertEquals(AmsAppointment.STATUS_PENDING_CONSUMPTION, appt.getStatus());
    }

    @Test
    void testMarkAsPaid_AlreadyPaid() {
        Long apptId = 1L;
        AmsAppointment appt = new AmsAppointment();
        appt.setId(apptId);
        appt.setPaymentStatus("PAID");

        when(appointmentMapper.selectById(apptId)).thenReturn(appt);

        assertThrows(BusinessException.class, () -> appointmentService.markAsPaid(apptId, "ADMIN"));
    }

    @Test
    void testRevokePayment_Success() {
        Long apptId = 1L;
        AmsAppointment appt = new AmsAppointment();
        appt.setId(apptId);
        appt.setPaymentStatus("PAID");
        appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
        appt.setTotalAmount(new BigDecimal("100.00"));
        appt.setStoreId(1L);
        appt.setCustomerId(100L);

        when(appointmentMapper.selectById(apptId)).thenReturn(appt);

        appointmentService.revokePayment(apptId, "Mistake", "ADMIN");

        verify(transactionMapper, times(1)).insert(any());
        verify(appointmentMapper, times(1)).update(any());
        assertEquals("UNPAID", appt.getPaymentStatus());
        assertEquals(AmsAppointment.STATUS_PENDING_PAYMENT, appt.getStatus());
    }

    @Test
    void testRevokePayment_CompletedOrder() {
        Long apptId = 1L;
        AmsAppointment appt = new AmsAppointment();
        appt.setId(apptId);
        appt.setPaymentStatus("PAID");
        appt.setStatus(AmsAppointment.STATUS_COMPLETED);
        appt.setTotalAmount(new BigDecimal("100.00"));
        appt.setStoreId(1L);
        appt.setCustomerId(100L);

        when(appointmentMapper.selectById(apptId)).thenReturn(appt);

        appointmentService.revokePayment(apptId, "Rollback", "ADMIN");

        // Verify rollback consumption called with negative amount
        verify(memberService, times(1)).accumulateConsumption(eq(100L), eq(new BigDecimal("-100.00")));
        verify(transactionMapper, times(1)).insert(any());
        assertEquals("UNPAID", appt.getPaymentStatus());
        assertEquals(AmsAppointment.STATUS_PENDING_PAYMENT, appt.getStatus());
    }
}
