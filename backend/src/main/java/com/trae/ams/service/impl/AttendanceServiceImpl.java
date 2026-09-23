package com.trae.ams.service.impl;

import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.dto.attendance.*;
import com.trae.ams.entity.AmsAttendance;
import com.trae.ams.entity.AmsLeaveRequest;
import com.trae.ams.mapper.AmsAttendanceMapper;
import com.trae.ams.mapper.AmsLeaveRequestMapper;
import com.trae.ams.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AmsLeaveRequestMapper leaveRequestMapper;

    @Autowired
    private AmsAttendanceMapper attendanceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyLeave(LeaveApplicationDTO dto, Long techId) {
        AmsLeaveRequest leave = new AmsLeaveRequest();
        leave.setStoreId(dto.getStoreId());
        leave.setTechId(techId);
        leave.setType(dto.getType());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setReason(dto.getReason());
        leave.setStatus("PENDING");
        leave.setCreateTime(LocalDateTime.now());
        leave.setUpdateTime(LocalDateTime.now());
        
        leaveRequestMapper.insert(leave);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveLeave(LeaveApprovalDTO dto, Long auditBy) {
        AmsLeaveRequest leave = leaveRequestMapper.selectById(dto.getId());
        if (leave == null) {
            throw new BusinessException("请假申请不存在");
        }
        
        leave.setStatus(dto.getStatus());
        leave.setAuditBy(auditBy);
        leave.setAuditTime(LocalDateTime.now());
        leave.setUpdateTime(LocalDateTime.now());
        
        leaveRequestMapper.update(leave);
    }

    @Override
    public List<AmsLeaveRequest> getLeaveList(LeaveQueryDTO query) {
        return leaveRequestMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clockIn(ClockInDTO dto, Long techId) {
        AmsAttendance attendance = new AmsAttendance();
        attendance.setStoreId(dto.getStoreId());
        attendance.setTechId(techId);
        attendance.setType(dto.getType());
        attendance.setLocation(dto.getLocation());
        
        LocalDateTime time = dto.getTime() != null ? dto.getTime() : LocalDateTime.now();
        attendance.setTime(time);
        attendance.setCreateTime(LocalDateTime.now());
        
        // Simple logic for status
        String status = "NORMAL";
        LocalTime clockTime = time.toLocalTime();
        
        if ("CLOCK_IN".equals(dto.getType())) {
            // Assume 9:30 is the latest start time
            if (clockTime.isAfter(LocalTime.of(9, 30))) {
                status = "LATE";
            }
        } else if ("CLOCK_OUT".equals(dto.getType())) {
            // Assume 18:00 is the earliest end time
            if (clockTime.isBefore(LocalTime.of(18, 0))) {
                status = "EARLY_LEAVE";
            }
        }
        attendance.setStatus(status);
        
        attendanceMapper.insert(attendance);
    }

    @Override
    public List<AmsAttendance> getAttendanceList(AttendanceQueryDTO query) {
        return attendanceMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAttendance(Long id, String status) {
        AmsAttendance attendance = new AmsAttendance();
        attendance.setId(id);
        attendance.setStatus(status);
        attendanceMapper.update(attendance);
    }
}
