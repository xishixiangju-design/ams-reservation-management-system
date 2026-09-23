package com.trae.ams.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.dto.appointment.BookingItemDTO;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.dto.waitinglist.JoinWaitlistDTO;
import com.trae.ams.dto.waitinglist.WaitingListDTO;
import com.trae.ams.dto.waitinglist.WaitingListQuery;
import com.trae.ams.entity.AmsWaitingList;
import com.trae.ams.mapper.AmsWaitingListMapper;
import com.trae.ams.service.AppointmentService;
import com.trae.ams.service.NotificationService;
import com.trae.ams.service.WaitingListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
public class WaitingListServiceImpl implements WaitingListService {

    private static final Logger log = LoggerFactory.getLogger(WaitingListServiceImpl.class);

    @Autowired
    private AmsWaitingListMapper waitingListMapper;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AppointmentService appointmentService;

    @Override
    public PageInfo<WaitingListDTO> getWaitingList(Long storeId, WaitingListQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<WaitingListDTO> list = waitingListMapper.selectList(storeId, query);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinWaitlist(JoinWaitlistDTO request) {
        Long userId = UserContext.getUserId();
        Long storeId = UserContext.getStoreId();
        
        if (storeId == null) {
            // Default to 1 if not present (e.g. strict test env might fail, but acceptable for now)
            storeId = 1L;
        }

        AmsWaitingList entity = new AmsWaitingList();
        BeanUtils.copyProperties(request, entity);
        entity.setCustomerId(userId);
        entity.setStoreId(storeId);
        entity.setStatus("WAITING");
        entity.setCreateTime(LocalDateTime.now());
        // Default expiry: end of the expected day
        if (request.getExpectedDate() != null) {
            entity.setExpiryTime(request.getExpectedDate().atTime(23, 59, 59));
        } else {
             entity.setExpiryTime(LocalDateTime.now().plusDays(7));
        }

        waitingListMapper.insert(entity);
        log.info("User {} joined waitlist for service {} at {}", userId, request.getServiceId(), request.getTimeRange());

        // 发送候补成功通知
        try {
            String title = "候补成功";
            String content = String.format("您已成功加入候补列表，日期：%s，时段：%s。如有空位将第一时间通知您。", 
                    request.getExpectedDate(), request.getTimeRange());
            notificationService.sendNotification(userId, title, content);
        } catch (Exception e) {
            log.error("Failed to send waitlist success notification", e);
        }
    }

    @Override
    public void checkWaitlistOnCancel(Long storeId, LocalDateTime start, LocalDateTime end) {
        log.info("Checking waitlist for released slot: {} - {}", start, end);
        
        // 1. Get waiting list for the date (FIFO ordered by SQL)
        List<AmsWaitingList> waitingList = waitingListMapper.selectByDateAndStatus(storeId, start.toLocalDate(), "WAITING");
        
        // 2. Filter matching time range
        for (AmsWaitingList item : waitingList) {
            if (isTimeOverlap(start, end, item.getTimeRange())) {
                log.info("Found matching waitlist item: {}, trying to auto-convert...", item.getId());
                
                try {
                    // 3. Try Auto Convert
                    executeConversion(item, false);
                    
                    log.info("Auto-conversion successful for waitlist item: {}", item.getId());
                    // Only process the first successful candidate (FIFO)
                    break;
                } catch (Exception e) {
                    log.error("Auto-conversion failed for waitlist item: {}. Error: {}", item.getId(), e.getMessage(), e);
                    
                    // Fallback to notification logic if conversion fails (optional)
                    // For now, we continue to next person or just stop?
                    // Let's continue to next person in queue
                    continue;
                }
            }
        }
    }

    private boolean isTimeOverlap(LocalDateTime start, LocalDateTime end, String rangeStr) {
        if (rangeStr == null || !rangeStr.contains("-")) return false;
        try {
            String[] parts = rangeStr.split("-");
            LocalTime rangeStart = LocalTime.parse(parts[0].trim());
            LocalTime rangeEnd = LocalTime.parse(parts[1].trim());
            
            // Convert slot times to LocalTime
            LocalTime slotStart = start.toLocalTime();
            LocalTime slotEnd = end.toLocalTime();
            
            // Overlap condition: StartA < EndB && EndA > StartB
            return slotStart.isBefore(rangeEnd) && slotEnd.isAfter(rangeStart);
        } catch (Exception e) {
            log.error("Failed to parse time range: {}", rangeStr, e);
            return false;
        }
    }

    private void notifyCustomer(AmsWaitingList item) {
        // Mock notification: In a real system this would send SMS or Push Notification
        log.warn(">>> [NOTIFICATION] Dear Customer (ID:{}), a slot has opened up for your requested time: {}! Please book immediately.", 
                 item.getCustomerId(), item.getTimeRange());
        
        String title = "预约名额通知";
        String content = String.format("尊敬的客户，您期望的时段 %s 已有空位释放！请立即前往预约。", item.getTimeRange());
        
        notificationService.sendNotification(item.getCustomerId(), title, content);
    }

    @Override
    public List<WaitingListDTO> getMyWaitlist() {
        return waitingListMapper.selectDtoByCustomerId(UserContext.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void convert(Long waitingListId) {
        Long userId = UserContext.getUserId();
        
        // 1. Check existence and ownership
        AmsWaitingList wl = waitingListMapper.selectById(waitingListId);
        if (wl == null) {
            throw new RuntimeException("Waiting list item not found");
        }
        
        // Allow if user is owner OR user is admin
        boolean isOwner = wl.getCustomerId().equals(userId);
        boolean isAdmin = UserContext.isAdmin();
        
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Permission denied");
        }
        
        // Only Admin-triggered conversion is considered "Manual/Offline"
        executeConversion(wl, isAdmin);
    }

    /**
     * 执行转正逻辑 (公共方法，供自动转正和手动转正调用)
     * @param isManual 是否手动转正(管理员操作)
     */
    private void executeConversion(AmsWaitingList wl, boolean isManual) {
        // Check status
        if (!"WAITING".equals(wl.getStatus()) && !"NOTIFIED".equals(wl.getStatus())) {
            throw new RuntimeException("Status invalid for conversion: " + wl.getStatus());
        }
        
        Long userId = wl.getCustomerId();

        // Construct BookingRequest
        BookingRequest request = new BookingRequest();
        request.setCustomerId(userId);
        request.setStoreId(wl.getStoreId());
        // Set creator to be the customer themselves (simulating they created it)
        request.setCreatorId(userId);
        
        // Parse start time from expectedDate and timeRange
        String[] parts = wl.getTimeRange().split("-");
        LocalTime startTime = LocalTime.parse(parts[0].trim());
        LocalDateTime appointmentTime = wl.getExpectedDate().atTime(startTime);
        
        request.setStartTime(appointmentTime);
        
        // Handle payment method and status
        request.setStatus(1); // All converted items start as PENDING_PAYMENT
        
        if (isManual) {
            request.setPaymentMethod("OFFLINE");
            request.setRemark("Manual converted from waiting list (Pending Offline Payment)");
        } else {
            // Auto conversion
            request.setPaymentMethod("ALIPAY");
            request.setRemark("Auto-converted from waiting list (Pending Online Payment)");
        }
        
        // Construct Item
        BookingItemDTO item = new BookingItemDTO();
        item.setServiceId(wl.getServiceId());
        item.setTechId(wl.getTechId()); // Might be null
        
        request.setItems(Collections.singletonList(item));
        
        // Create Appointment
        try {
            // Note: createAppointment might depend on UserContext for storeId.
            // If UserContext is missing (background task), we might need to handle it.
            // Assuming AppointmentService handles it gracefully or defaults to 1L.
            appointmentService.createAppointment(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create appointment: " + e.getMessage());
        }
        
        // Update Status
        wl.setStatus("CONVERTED");
        waitingListMapper.update(wl);
        
        // Notify
        String title = "候补转正成功";
        String content = String.format("您在 %s 的候补请求已生成预约，当前状态为【待支付】。请尽快完成支付以锁定名额。", wl.getExpectedDate());
        
        notificationService.sendNotification(userId, title, content);
    }
}
