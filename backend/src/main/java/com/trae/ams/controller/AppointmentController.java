package com.trae.ams.controller;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.result.Result;
import com.trae.ams.dto.appointment.BatchBookingRequest;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.dto.appointment.SlotQueryRequest;
import com.trae.ams.dto.appointment.TimeSlotDTO;
import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.service.AppointmentService;
import com.trae.ams.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    private boolean currentUserIsAdmin() {
        Long userId = UserContext.getUserId();
        if (userId == null) return false;
        List<String> roles = sysUserRoleMapper.selectRoleCodesByUserId(userId);
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    @PostMapping("/slots")
    public Result<List<TimeSlotDTO>> getSlots(@RequestBody SlotQueryRequest request) {
        return Result.success(appointmentService.getAvailableSlots(request));
    }

    /**
     * 客户自助预约
     */
    @PostMapping
    public Result<Long> create(@RequestBody BookingRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        request.setCustomerId(userId);
        return Result.success(appointmentService.createAppointment(request));
    }

    /**
     * 管理员代客预约
     */
    @PostMapping("/admin/create")
    public Result<Long> createByAdmin(@RequestBody BookingRequest request) {
        // Admin can specify customerId in request body
        if (request.getCustomerId() == null) {
            // If no customer selected, maybe fallback to a Guest account or error
            // For now, assume frontend passes a valid customerId (e.g. selected from list)
             throw new RuntimeException("必须选择客户");
        }
        return Result.success(appointmentService.createAppointment(request));
    }



    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return Result.success();
    }

    /**
     * 完成订单
     * 前置条件：订单状态必须为 5 (待消费)
     */
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        appointmentService.completeAppointment(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AmsAppointment> get(@PathVariable Long id) {
        return Result.success(appointmentService.getAppointment(id));
    }

    @GetMapping("/me")
    public Result<com.github.pagehelper.PageInfo<AmsAppointment>> listMy(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
             throw new RuntimeException("未登录");
        }
        LocalDateTime start = startDate != null ? DateUtil.parse(startDate).toLocalDateTime() : null;
        LocalDateTime end = endDate != null ? DateUtil.parse(endDate).toLocalDateTime() : null;
        if (end != null && end.toLocalTime().equals(java.time.LocalTime.MIN)) {
            end = end.with(java.time.LocalTime.MAX);
        }
        return Result.success(appointmentService.listMyAppointments(userId, pageNum, pageSize, start, end, status));
    }

    @GetMapping("/technician/me")
    public Result<com.github.pagehelper.PageInfo<AmsAppointment>> listMyAssignments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
             throw new RuntimeException("未登录");
        }
        LocalDateTime start = startDate != null ? DateUtil.parse(startDate).toLocalDateTime() : null;
        LocalDateTime end = endDate != null ? DateUtil.parse(endDate).toLocalDateTime() : null;
        if (end != null && end.toLocalTime().equals(java.time.LocalTime.MIN)) {
            end = end.with(java.time.LocalTime.MAX);
        }
        return Result.success(appointmentService.listTechnicianAppointments(userId, pageNum, pageSize, start, end, status));
    }
    
    /**
     * 管理端列表查询
     */
    @GetMapping("/admin/list")
    public Result<com.github.pagehelper.PageInfo<AmsAppointment>> listAdmin(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Long storeId = UserContext.getStoreIdOrDefault();
        LocalDateTime start = startDate != null ? DateUtil.parse(startDate).toLocalDateTime() : null;
        LocalDateTime end = endDate != null ? DateUtil.parse(endDate).toLocalDateTime() : null;
        if (end != null && end.toLocalTime().equals(java.time.LocalTime.MIN)) {
            end = end.with(java.time.LocalTime.MAX);
        }
        
        return Result.success(appointmentService.listAppointments(storeId, pageNum, pageSize, start, end, status, keyword));
    }

    @GetMapping("/admin/customer/{customerId}")
    public Result<com.github.pagehelper.PageInfo<AmsAppointment>> listByCustomerForAdmin(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // Reuse listMyAppointments logic but for specific customer
        return Result.success(appointmentService.listMyAppointments(customerId, pageNum, pageSize, null, null, null));
    }

    @PostMapping("/{id}/reschedule")
    public Result<Void> reschedule(@PathVariable Long id, @RequestBody RescheduleRequest request) {
        appointmentService.reschedule(id, request.getNewStartTime(), request.getNewTechId());
        return Result.success();
    }

    public static class RescheduleRequest {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime newStartTime;
        private Long newTechId;

        public LocalDateTime getNewStartTime() {
            return newStartTime;
        }

        public void setNewStartTime(LocalDateTime newStartTime) {
            this.newStartTime = newStartTime;
        }

        public Long getNewTechId() {
            return newTechId;
        }

        public void setNewTechId(Long newTechId) {
            this.newTechId = newTechId;
        }
    }

    @PostMapping("/admin/batch")
    public Result<List<Long>> batchCreate(@RequestBody BatchBookingRequest request) {
        if (!currentUserIsAdmin()) {
            throw new RuntimeException("无权操作");
        }
        // Force offline payment for admin batch creation
        request.setSource("ADMIN");
        request.setPaymentMethod("OFFLINE");
        request.setCreatorId(UserContext.getUserId());
        
        return Result.success(appointmentService.batchCreateAppointment(request));
    }

    @PostMapping("/admin/batch/pay")
    public Result<Void> batchPay(@RequestBody List<Long> appointmentIds) {
        if (!currentUserIsAdmin()) {
            throw new RuntimeException("无权操作");
        }
        appointmentService.batchPay(appointmentIds);
        return Result.success();
    }

    @PostMapping("/batch/cancel")
    public Result<Void> batchCancel(@RequestBody List<Long> appointmentIds) {
        appointmentService.batchCancel(appointmentIds);
        return Result.success();
    }
}
