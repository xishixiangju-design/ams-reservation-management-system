package com.trae.ams.service;

import com.trae.ams.dto.attendance.*;
import com.trae.ams.entity.AmsAttendance;
import com.trae.ams.entity.AmsLeaveRequest;

import java.util.List;

public interface AttendanceService {
    /**
     * 申请请假
     */
    void applyLeave(LeaveApplicationDTO dto, Long techId);

    /**
     * 审批请假
     */
    void approveLeave(LeaveApprovalDTO dto, Long auditBy);

    /**
     * 查询请假列表
     */
    List<AmsLeaveRequest> getLeaveList(LeaveQueryDTO query);

    /**
     * 打卡
     */
    void clockIn(ClockInDTO dto, Long techId);

    /**
     * 查询打卡记录
     */
    List<AmsAttendance> getAttendanceList(AttendanceQueryDTO query);

    /**
     * 更新打卡记录（如标记异常）
     */
    void updateAttendance(Long id, String status);
}
