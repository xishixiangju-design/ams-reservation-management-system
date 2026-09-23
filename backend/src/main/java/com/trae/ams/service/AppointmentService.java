package com.trae.ams.service;

import com.github.pagehelper.PageInfo;
import com.trae.ams.dto.appointment.BatchBookingRequest;
import com.trae.ams.dto.appointment.BookingRequest;
import com.trae.ams.dto.appointment.SlotQueryRequest;
import com.trae.ams.dto.appointment.TimeSlotDTO;
import com.trae.ams.entity.AmsAppointment;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    /**
     * 计算可用时间槽
     */
    List<TimeSlotDTO> getAvailableSlots(SlotQueryRequest request);

    /**
     * 创建预约 (下单)
     */
    Long createAppointment(BookingRequest request);

    /**
     * 确认预约 (已废弃)
     */
    // void confirmAppointment(Long appointmentId);

    /**
     * 取消预约 (包含12h违约判定)
     */
    void cancelAppointment(Long appointmentId);

    /**
     * 完成预约 (更新技师上次服务时间)
     */
    void completeAppointment(Long appointmentId);

    /**
     * 获取预约详情
     */
    AmsAppointment getAppointment(Long id);

    /**
     * 获取我的预约列表 (分页 + 筛选)
     */
    PageInfo<AmsAppointment> listMyAppointments(Long userId, Integer pageNum, Integer pageSize, LocalDateTime startDate, LocalDateTime endDate, Integer status);

    /**
     * 获取技师的预约列表 (分页 + 筛选)
     */
    PageInfo<AmsAppointment> listTechnicianAppointments(Long techId, Integer pageNum, Integer pageSize, LocalDateTime startDate, LocalDateTime endDate, Integer status);

    /**
     * 管理端查询列表 (分页)
     */
    PageInfo<AmsAppointment> listAppointments(Long storeId, Integer pageNum, Integer pageSize, LocalDateTime startDate, LocalDateTime endDate, Integer status, String keyword);

    /**
     * 批量创建预约 (Admin only)
     * @return List of created appointment IDs
     */
    List<Long> batchCreateAppointment(BatchBookingRequest request);

    /**
     * 批量支付确认 (Status -> Pending Consumption)
     */
    void batchPay(List<Long> appointmentIds);

    /**
     * 批量取消预约
     */
    void batchCancel(List<Long> appointmentIds);

    /**
     * 预约改期
     * @param appointmentId 预约ID
     * @param newStartTime 新开始时间
     * @param newTechId 新技师ID (可选)
     */
    void reschedule(Long appointmentId, LocalDateTime newStartTime, Long newTechId);

    /**
     * 管理员标记已付款 (线下付款)
     */
    void markAsPaid(Long appointmentId, String operatorId);

    /**
     * 管理员撤销付款 (支持已完成订单的回滚)
     */
    void revokePayment(Long appointmentId, String reason, String operatorId);
}
