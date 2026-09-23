package com.trae.ams.service.impl;

import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.appointment.BookingItemDTO;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.entity.*;
import com.trae.ams.mapper.*;
import com.trae.ams.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Mock
    private AmsAppointmentMapper appointmentMapper;
    @Mock
    private AmsAppointmentItemMapper appointmentItemMapper;
    @Mock
    private AmsServiceMapper serviceMapper;
    @Mock
    private AmsTechnicianInfoMapper technicianMapper;
    @Mock
    private AmsRoomMapper roomMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private AmsViolationRecordMapper violationMapper;
    @Mock
    private TechnicianShiftService shiftService;
    @Mock
    private MemberService memberService;
    @Mock
    private PricingService pricingService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AmsComboRuleMapper comboRuleMapper;
    @Mock
    private WaitingListService waitingListService;

    private static final Long STORE_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long TECH_ID_A = 201L;
    private static final Long TECH_ID_B = 202L;
    private static final Long ROOM_ID_1 = 301L;
    private static final Long SERVICE_ID = 10L;

    @BeforeEach
    void setUp() {
        UserContext.setStoreId(STORE_ID);
        UserContext.setUserId(USER_ID);
        UserContext.setIsAdmin(false);
        // Ensure waitingListService is injected (handling potential @Lazy issue or Mockito quirk)
        ReflectionTestUtils.setField(appointmentService, "waitingListService", waitingListService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("TC001: 正常预约 (单人) - 成功")
    void testCreateAppointment_Success() {
        // Setup Mocks
        BookingRequest request = new BookingRequest();
        request.setStoreId(STORE_ID);
        request.setCustomerId(USER_ID);
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        request.setPeopleCount(1);
        
        BookingItemDTO item = new BookingItemDTO();
        item.setServiceId(SERVICE_ID);
        item.setTechId(TECH_ID_A);
        request.setItems(Collections.singletonList(item));

        // Mock Service
        AmsService service = new AmsService();
        service.setId(SERVICE_ID);
        service.setDuration(60);
        service.setPrice(new BigDecimal("100.00"));
        when(serviceMapper.selectById(SERVICE_ID)).thenReturn(service);
        // when(serviceMapper.selectByIds(anyList())).thenReturn(Collections.singletonList(service)); // Unnecessary

        // Mock Conflicts (Empty)
        when(appointmentMapper.selectConflictAppointments(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Mock Room
        AmsRoom room = new AmsRoom();
        room.setId(ROOM_ID_1);
        room.setCapacity(1);
        when(roomMapper.selectList(anyLong(), eq(1))).thenReturn(Collections.singletonList(room));

        // Mock Technician
        AmsTechnicianInfo tech = new AmsTechnicianInfo();
        tech.setUserId(TECH_ID_A);
        tech.setStatus("ACTIVE");
        when(technicianMapper.selectList(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(tech));
        when(shiftService.isTechWorking(anyLong(), any(), any())).thenReturn(true);

        // Mock Pricing
        when(memberService.calculatePrice(anyLong(), any())).thenReturn(new BigDecimal("100.00"));
        when(pricingService.calculateServicePrice(anyLong(), anyInt(), any())).thenReturn(new BigDecimal("100.00"));

        // Mock Insert to set ID
        doAnswer(invocation -> {
            AmsAppointment appt = invocation.getArgument(0);
            appt.setId(12345L);
            return 1;
        }).when(appointmentMapper).insert(any(AmsAppointment.class));

        // Execute
        Long apptId = appointmentService.createAppointment(request);

        // Verify
        assertNotNull(apptId); 
        assertEquals(12345L, apptId);
        
        verify(appointmentMapper).insert(any(AmsAppointment.class));
        verify(appointmentItemMapper).batchInsert(anyList());
    }

    @Test
    @DisplayName("TC002: 冲突检测 (技师占用) - 失败")
    void testCreateAppointment_Conflict_TechOccupied() {
        BookingRequest request = new BookingRequest();
        request.setStoreId(STORE_ID);
        request.setCustomerId(USER_ID);
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        request.setStartTime(start);
        request.setPeopleCount(1);
        
        BookingItemDTO item = new BookingItemDTO();
        item.setServiceId(SERVICE_ID);
        item.setTechId(TECH_ID_A);
        request.setItems(Collections.singletonList(item));

        // Mock Service
        AmsService service = new AmsService();
        service.setId(SERVICE_ID);
        service.setDuration(60);
        service.setPrice(new BigDecimal("100.00"));
        when(serviceMapper.selectById(SERVICE_ID)).thenReturn(service);

        // Mock Pricing (Fix NPE)
        when(memberService.calculatePrice(anyLong(), any())).thenReturn(new BigDecimal("100.00"));
        when(pricingService.calculateServicePrice(anyLong(), anyInt(), any())).thenReturn(new BigDecimal("100.00"));

        // Mock Conflicts (Tech A is occupied)
        AmsAppointment conflictAppt = new AmsAppointment();
        conflictAppt.setId(999L);
        // Ensure time overlaps
        conflictAppt.setStartTime(start.minusMinutes(30));
        conflictAppt.setEndTime(start.plusMinutes(30));
        
        when(appointmentMapper.selectConflictAppointments(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(conflictAppt));
        
        AmsAppointmentItem conflictItem = new AmsAppointmentItem();
        conflictItem.setApptId(999L); // Ensure ID matches for grouping
        conflictItem.setTechId(TECH_ID_A);
        when(appointmentItemMapper.selectByApptIds(anyList())).thenReturn(Collections.singletonList(conflictItem));

        // Mock Room
        AmsRoom room = new AmsRoom();
        room.setId(ROOM_ID_1);
        room.setCapacity(1);
        when(roomMapper.selectList(anyLong(), eq(1))).thenReturn(Collections.singletonList(room));

        // Mock Technician
        AmsTechnicianInfo tech = new AmsTechnicianInfo();
        tech.setUserId(TECH_ID_A);
        tech.setStatus("ACTIVE");
        when(technicianMapper.selectList(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(tech));
        when(shiftService.isTechWorking(anyLong(), any(), any())).thenReturn(true);

        // Execute & Verify
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            appointmentService.createAppointment(request);
        });
        assertTrue(exception.getMessage().contains("指定技师") && exception.getMessage().contains("已忙碌"));
    }

    @Test
    @DisplayName("TC005: 轮牌自动分配 - LRU 策略")
    void testAutoAssignTechnician_RoundRobin() {
        BookingRequest request = new BookingRequest();
        request.setStoreId(STORE_ID);
        request.setCustomerId(USER_ID);
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        request.setStartTime(start);
        request.setPeopleCount(1);
        
        // No Tech ID specified -> Auto Assign
        BookingItemDTO item = new BookingItemDTO();
        item.setServiceId(SERVICE_ID);
        item.setTechId(null); 
        request.setItems(Collections.singletonList(item));

        // Mock Service
        AmsService service = new AmsService();
        service.setId(SERVICE_ID);
        service.setDuration(60);
        when(serviceMapper.selectById(SERVICE_ID)).thenReturn(service);

        // Mock Conflicts (None)
        when(appointmentMapper.selectConflictAppointments(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Mock Room
        AmsRoom room = new AmsRoom();
        room.setId(ROOM_ID_1);
        room.setCapacity(1);
        when(roomMapper.selectList(anyLong(), eq(1))).thenReturn(Collections.singletonList(room));

        // Mock Technicians: A (LastJob: 10:00), B (LastJob: 09:00)
        // Expected: B should be chosen (Earlier LastJob)
        AmsTechnicianInfo techA = new AmsTechnicianInfo();
        techA.setUserId(TECH_ID_A);
        techA.setStatus("ACTIVE");
        techA.setLastJobEndTime(LocalDateTime.of(2023, 1, 1, 10, 0));

        AmsTechnicianInfo techB = new AmsTechnicianInfo();
        techB.setUserId(TECH_ID_B);
        techB.setStatus("ACTIVE");
        techB.setLastJobEndTime(LocalDateTime.of(2023, 1, 1, 9, 0));

        List<AmsTechnicianInfo> techs = new ArrayList<>();
        techs.add(techA);
        techs.add(techB);

        when(technicianMapper.selectList(anyLong(), any(), any())).thenReturn(techs);
        when(shiftService.isTechWorking(anyLong(), any(), any())).thenReturn(true);

        // Mock Pricing
        when(memberService.calculatePrice(anyLong(), any())).thenReturn(BigDecimal.ZERO);
        when(pricingService.calculateServicePrice(anyLong(), anyInt(), any())).thenReturn(BigDecimal.ZERO);

        // Execute
        appointmentService.createAppointment(request);

        // Verify: Item should be assigned to Tech B
        verify(appointmentItemMapper).batchInsert(argThat(list -> {
            AmsAppointmentItem savedItem = list.get(0);
            return savedItem.getTechId().equals(TECH_ID_B);
        }));
        
        // Verify: Tech B's LastJobEndTime is updated
        verify(technicianMapper).update(argThat(t -> 
            t.getUserId().equals(TECH_ID_B) && t.getLastJobEndTime() != null
        ));
    }

    @Test
    @DisplayName("TC011: 违约取消 (<12h) - 记录违约")
    void testCancelAppointment_LateCancellation() {
        Long apptId = 100L;
        AmsAppointment appt = new AmsAppointment();
        appt.setId(apptId);
        appt.setStoreId(STORE_ID);
        appt.setCustomerId(USER_ID);
        appt.setStartTime(LocalDateTime.now().plusHours(10)); // 10 hours from now (<12h)
        appt.setStatus(AmsAppointment.STATUS_PENDING_CONSUMPTION);
        appt.setPaymentStatus("UNPAID");

        when(appointmentMapper.selectById(apptId)).thenReturn(appt);
        
        // Mock User for Violation Count
        SysUser user = new SysUser();
        user.setId(USER_ID);
        user.setViolationCount(0);
        when(sysUserMapper.selectById(USER_ID)).thenReturn(user);

        // Execute
        appointmentService.cancelAppointment(apptId);

        // Verify
        // 1. Status -> VIOLATION (4)
        verify(appointmentMapper).updateStatus(eq(apptId), eq(AmsAppointment.STATUS_VIOLATION));
        
        // 2. Violation Record Created
        verify(violationMapper).insert(any(AmsViolationRecord.class));
        
        // 3. User Violation Count Incremented
        verify(sysUserMapper).update(argThat(u -> u.getViolationCount() == 1));
    }
}
