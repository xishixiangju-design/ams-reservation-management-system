package com.trae.ams.controller;

import com.trae.ams.common.annotation.Log;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.result.Result;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/appointment")
public class AdminAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 管理员新增预约 (强制线下支付)
     */
    @PostMapping("/create")
    @Log(title = "预约管理", businessType = "INSERT")
    public Result<Long> create(@RequestBody BookingRequest request) {
        // 强制设置
        request.setSource("ADMIN");
        request.setPaymentMethod("OFFLINE");
        request.setCreatorId(UserContext.getUserId());
        
        Long id = appointmentService.createAppointment(request);
        return Result.success(id);
    }

    /**
     * 标记为已付款
     */
    @PostMapping("/{id}/pay")
    @Log(title = "预约管理", businessType = "UPDATE")
    public Result<Void> markAsPaid(@PathVariable Long id) {
        // 校验权限
        if (!UserContext.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        
        appointmentService.markAsPaid(id, String.valueOf(UserContext.getUserId()));
        return Result.success();
    }

    /**
     * 标记为已完成
     */
    @PostMapping("/{id}/complete")
    @Log(title = "预约管理", businessType = "UPDATE")
    public Result<Void> markAsCompleted(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        appointmentService.completeAppointment(id);
        return Result.success();
    }

    /**
     * 撤销付款
     */
    @PostMapping("/{id}/revoke-pay")
    @Log(title = "预约管理", businessType = "UPDATE")
    public Result<Void> revokePayment(@PathVariable Long id, @RequestParam String reason) {
        if (!UserContext.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        appointmentService.revokePayment(id, reason, String.valueOf(UserContext.getUserId()));
        return Result.success();
    }
}
