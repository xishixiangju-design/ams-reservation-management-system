package com.trae.ams.service.impl;

import com.trae.ams.common.context.UserContext;
import com.trae.ams.dto.dashboard.DashboardSummaryDTO;
import com.trae.ams.dto.dashboard.RoomStatusDTO;
import com.trae.ams.dto.dashboard.TechStatusDTO;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.entity.AmsAppointmentItem;
import com.trae.ams.entity.AmsRoom;
import com.trae.ams.entity.AmsTechnicianInfo;
import com.trae.ams.mapper.*;
import com.trae.ams.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.time.format.DateTimeFormatter;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired private AmsAppointmentMapper appointmentMapper;
    @Autowired private AmsWaitingListMapper waitingListMapper;
    @Autowired private AmsRoomMapper roomMapper;
    @Autowired private AmsTechnicianInfoMapper techMapper;
    @Autowired private AmsAppointmentItemMapper itemMapper;

    @Override
    public List<Map<String, Object>> getCalendarStats(String startDate, String endDate) {
        Long storeId = UserContext.getStoreIdOrDefault();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, fmt);
        LocalDateTime end = LocalDateTime.parse(endDate, fmt);
        
        return appointmentMapper.selectCalendarStats(storeId, start, end);
    }

    @Override
    public DashboardSummaryDTO getSummary() {
        Long storeId = UserContext.getStoreIdOrDefault();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        DashboardSummaryDTO dto = new DashboardSummaryDTO();
        dto.setTodayAppointments(appointmentMapper.countByDate(storeId, start, end));
        
        // 待服务：统计 status IN (0: PENDING, 1: CONFIRMED, 5: PENDING_CONSUMPTION)
        List<Integer> pendingStatuses = Arrays.asList(0, 1, 5);
        dto.setPendingServicesCount(appointmentMapper.countByDateAndStatusList(storeId, start, end, pendingStatuses));
        // 今日营收：按支付成功统计
        dto.setTodayRevenue(appointmentMapper.sumPaidAmountByDate(storeId, start, end));
        
        dto.setWaitlistCount(waitingListMapper.countByStoreIdAndStatus(storeId, "WAITING"));
        dto.setViolationCount(appointmentMapper.countByDateAndStatus(storeId, start, end, 4));

        return dto;
    }

    @Override
    public List<RoomStatusDTO> getRoomStatus() {
        Long storeId = UserContext.getStoreIdOrDefault();
        LocalDateTime now = LocalDateTime.now();

        List<AmsRoom> rooms = roomMapper.selectList(storeId, null);
        List<AmsAppointment> activeAppts = appointmentMapper.selectActiveAppointments(storeId, now);
        List<Long> apptIds = activeAppts.stream().map(AmsAppointment::getId).collect(Collectors.toList());
        
        Map<Long, AmsAppointmentItem> roomItemMap = new HashMap<>();
        Map<Long, AmsAppointment> itemApptMap = new HashMap<>();

        if (!apptIds.isEmpty()) {
            List<AmsAppointmentItem> items = itemMapper.selectByApptIds(apptIds);
            for (AmsAppointmentItem item : items) {
                if (item.getRoomId() != null) {
                    roomItemMap.put(item.getRoomId(), item);
                    AmsAppointment appt = activeAppts.stream().filter(a -> a.getId().equals(item.getApptId())).findFirst().orElse(null);
                    if (appt != null) {
                        itemApptMap.put(item.getId(), appt);
                    }
                }
            }
        }

        List<RoomStatusDTO> dtos = new ArrayList<>();
        for (AmsRoom room : rooms) {
            RoomStatusDTO dto = new RoomStatusDTO();
            dto.setId(room.getId());
            dto.setName(room.getName());
            dto.setType(room.getType());
            
            if (room.getStatus() == 0) {
                dto.setStatus("CLEANING");
            } else if (roomItemMap.containsKey(room.getId())) {
                dto.setStatus("OCCUPIED");
                AmsAppointmentItem item = roomItemMap.get(room.getId());
                AmsAppointment appt = itemApptMap.get(item.getId());
                if (appt != null) {
                    dto.setCurrentCustomer(appt.getContactName());
                    long remainingMin = Duration.between(now, appt.getEndTime()).toMinutes();
                    dto.setRemainingTime(remainingMin > 0 ? remainingMin + " min" : "Overtime");
                }
            } else {
                dto.setStatus("IDLE");
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public List<TechStatusDTO> getTechStatus() {
        Long storeId = UserContext.getStoreIdOrDefault();
        LocalDateTime now = LocalDateTime.now();

        List<AmsTechnicianInfo> techs = techMapper.selectList(storeId, null, null);
        List<AmsAppointment> activeAppts = appointmentMapper.selectActiveAppointments(storeId, now);
        List<Long> apptIds = activeAppts.stream().map(AmsAppointment::getId).collect(Collectors.toList());
        
        Map<Long, AmsAppointmentItem> techItemMap = new HashMap<>();
        Map<Long, AmsAppointment> itemApptMap = new HashMap<>();

        if (!apptIds.isEmpty()) {
            List<AmsAppointmentItem> items = itemMapper.selectByApptIds(apptIds);
            for (AmsAppointmentItem item : items) {
                if (item.getTechId() != null) {
                    techItemMap.put(item.getTechId(), item);
                    AmsAppointment appt = activeAppts.stream().filter(a -> a.getId().equals(item.getApptId())).findFirst().orElse(null);
                    if (appt != null) {
                        itemApptMap.put(item.getId(), appt);
                    }
                }
            }
        }

        List<TechStatusDTO> dtos = new ArrayList<>();
        for (AmsTechnicianInfo tech : techs) {
            TechStatusDTO dto = new TechStatusDTO();
            dto.setId(tech.getUserId());
            dto.setName(tech.getRealName());
            dto.setLevel(tech.getLevel());
            dto.setWheelSeq(tech.getWheelSeq());
            
            if ("LEAVE".equals(tech.getStatus())) {
                dto.setStatus("LEAVE");
            } else if (techItemMap.containsKey(tech.getUserId())) {
                dto.setStatus("BUSY");
                AmsAppointmentItem item = techItemMap.get(tech.getUserId());
                AmsAppointment appt = itemApptMap.get(item.getId());
                if (appt != null) {
                    long remainingMin = Duration.between(now, appt.getEndTime()).toMinutes();
                    dto.setCurrentTask("Serving " + appt.getContactName() + " (" + (remainingMin > 0 ? remainingMin + "m" : "Over") + ")");
                }
            } else {
                dto.setStatus("IDLE");
            }
            dtos.add(dto);
        }
        return dtos;
    }
}
