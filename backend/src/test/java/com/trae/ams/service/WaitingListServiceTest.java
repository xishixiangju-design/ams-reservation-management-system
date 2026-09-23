package com.trae.ams.service;

import com.trae.ams.common.context.UserContext;
import com.trae.ams.dto.waitinglist.JoinWaitlistDTO;
import com.trae.ams.dto.waitinglist.WaitingListDTO;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.entity.AmsService;
import com.trae.ams.entity.AmsWaitingList;
import com.trae.ams.entity.SysUser;
import com.trae.ams.mapper.AmsAppointmentMapper;
import com.trae.ams.mapper.AmsServiceMapper;
import com.trae.ams.mapper.AmsWaitingListMapper;
import com.trae.ams.mapper.SysUserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest
@Transactional
public class WaitingListServiceTest {

    @Autowired
    private WaitingListService waitingListService;

    @Autowired
    private AmsWaitingListMapper waitingListMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AmsServiceMapper serviceMapper;

    @Autowired
    private AmsAppointmentMapper appointmentMapper;

    @Test
    public void testJoinAndNotify() {
        // Prepare data
        SysUser user = new SysUser();
        user.setUsername("test_user_" + System.currentTimeMillis());
        user.setNickname("Test User");
        user.setPassword("123456");
        user.setSalt("salt");
        user.setStatus(1);
        user.setMembershipLevel("NORMAL");
        sysUserMapper.insert(user);
        Long userId = user.getId();

        AmsService service = new AmsService();
        service.setName("Test Service");
        service.setDuration(60);
        service.setPrice(BigDecimal.TEN);
        service.setStoreId(1L);
        service.setStatus(1);
        serviceMapper.insert(service);
        Long serviceId = service.getId();

        try {
            UserContext.setUserId(userId);
            UserContext.setStoreId(1L);

            LocalDate date = LocalDate.now().plusDays(1);
            String timeRange = "14:00-15:00";

            // 1. Join
            JoinWaitlistDTO joinReq = new JoinWaitlistDTO();
            joinReq.setServiceId(serviceId);
            joinReq.setExpectedDate(date);
            joinReq.setTimeRange(timeRange);
            joinReq.setPeopleCount(1);
            
            waitingListService.joinWaitlist(joinReq);

            List<WaitingListDTO> my = waitingListService.getMyWaitlist();
            Assertions.assertFalse(my.isEmpty());
            // Since there might be other data, filter by our user
            AmsWaitingList entry = my.stream()
                .filter(e -> e.getCustomerId().equals(userId))
                .findFirst().orElse(null);
            
            Assertions.assertNotNull(entry);
            Assertions.assertEquals("WAITING", entry.getStatus());

            // 2. Check overlap (Exact match)
            LocalDateTime start = LocalDateTime.of(date, LocalTime.of(14, 0));
            LocalDateTime end = LocalDateTime.of(date, LocalTime.of(15, 0));
            
            waitingListService.checkWaitlistOnCancel(1L, start, end);
            
            // Verify status changed to NOTIFIED or CONVERTED
            AmsWaitingList updated = waitingListMapper.selectById(entry.getId());
            Assertions.assertTrue("CONVERTED".equals(updated.getStatus()) || "NOTIFIED".equals(updated.getStatus()));

            if ("CONVERTED".equals(updated.getStatus())) {
                // Check if appointment is created
                List<AmsAppointment> appointments = appointmentMapper.selectByCustomer(userId, null, null, null);
                Assertions.assertFalse(appointments.isEmpty(), "Appointment should be created upon conversion");
                AmsAppointment appt = appointments.get(0);
                Assertions.assertEquals(userId, appt.getCustomerId());
                // Verify Status is 5 (Pending Consumption)
                Assertions.assertEquals(5, appt.getStatus());
                // Verify Creator is the customer (userId)
                Assertions.assertEquals(userId, appt.getCreatorId());
            }

        } finally {
            UserContext.clear();
        }
    }

    @Test
    public void testOverlapLogic() {
        // Prepare data
        SysUser user = new SysUser();
        user.setUsername("test_user_2_" + System.currentTimeMillis());
        user.setNickname("Test User 2");
        user.setPassword("123456");
        user.setSalt("salt");
        user.setStatus(1);
        user.setMembershipLevel("NORMAL");
        sysUserMapper.insert(user);
        Long userId = user.getId();

        AmsService service = new AmsService();
        service.setName("Test Service 2");
        service.setDuration(60);
        service.setPrice(BigDecimal.TEN);
        service.setStoreId(1L);
        service.setStatus(1);
        serviceMapper.insert(service);
        Long serviceId = service.getId();

        // Test partial overlap
        // Request: 14:00-16:00
        // Cancelled: 15:00-15:30 -> Should overlap?
        // My logic: SlotStart < RangeEnd && SlotEnd > RangeStart
        // 15:00 < 16:00 && 15:30 > 14:00 -> True.
        
        try {
            UserContext.setUserId(userId);
            UserContext.setStoreId(1L);

            LocalDate date = LocalDate.now().plusDays(2);
            String timeRange = "14:00-16:00";

            JoinWaitlistDTO joinReq = new JoinWaitlistDTO();
            joinReq.setServiceId(serviceId);
            joinReq.setExpectedDate(date);
            joinReq.setTimeRange(timeRange);
            joinReq.setPeopleCount(1);
            
            waitingListService.joinWaitlist(joinReq);
            
            List<WaitingListDTO> my = waitingListService.getMyWaitlist();
            AmsWaitingList entry = my.stream()
                .filter(e -> e.getCustomerId().equals(userId))
                .findFirst().orElse(null);
            Assertions.assertNotNull(entry);

            // Cancelled slot: 15:00-15:30
            LocalDateTime start = LocalDateTime.of(date, LocalTime.of(15, 0));
            LocalDateTime end = LocalDateTime.of(date, LocalTime.of(15, 30));
            
            waitingListService.checkWaitlistOnCancel(1L, start, end);
            
            AmsWaitingList updated = waitingListMapper.selectById(entry.getId());
             // Similarly, expect CONVERTED or NOTIFIED
            Assertions.assertTrue("CONVERTED".equals(updated.getStatus()) || "NOTIFIED".equals(updated.getStatus()));
            
        } finally {
            UserContext.clear();
        }
    }
}
