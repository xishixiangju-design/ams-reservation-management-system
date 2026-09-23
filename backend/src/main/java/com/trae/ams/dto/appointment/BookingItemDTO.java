package com.trae.ams.dto.appointment;

import java.io.Serializable;

/**
 * 预约明细 DTO
 */
public class BookingItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 服务项目ID */
    private Long serviceId;

    /** 指定技师ID (可选) */
    private Long techId;

    /** 指定房间ID (可选，通常系统自动分配) */
    private Long roomId;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getTechId() {
        return techId;
    }

    public void setTechId(Long techId) {
        this.techId = techId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
