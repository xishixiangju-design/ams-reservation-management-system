package com.trae.ams.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预约明细表
 */
public class AmsAppointmentItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 关联主订单ID */
    private Long apptId;

    /** 服务项目ID */
    private Long serviceId;

    /** 技师ID */
    private Long techId;

    /** 房间ID */
    private Long roomId;

    /** 明细金额 */
    private BigDecimal price;

    // 关联字段
    private AmsService service;
    private AmsTechnicianInfo technician;
    private AmsRoom room;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApptId() {
        return apptId;
    }

    public void setApptId(Long apptId) {
        this.apptId = apptId;
    }

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public AmsService getService() {
        return service;
    }

    public void setService(AmsService service) {
        this.service = service;
    }

    public AmsTechnicianInfo getTechnician() {
        return technician;
    }

    public void setTechnician(AmsTechnicianInfo technician) {
        this.technician = technician;
    }

    public AmsRoom getRoom() {
        return room;
    }

    public void setRoom(AmsRoom room) {
        this.room = room;
    }
}
