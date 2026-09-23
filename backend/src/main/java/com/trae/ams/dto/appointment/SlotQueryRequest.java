package com.trae.ams.dto.appointment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 时间槽查询请求
 */
public class SlotQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 预约日期 (必填) */
    private LocalDate date;

    /** 指定技师ID (可选, 若指定则只看该技师) */
    private Long techId;

    /** 服务项目ID (必填, 用于计算时长) - 兼容旧版 */
    private Long serviceId;

    /** 多选服务ID列表 (新版使用) */
    private java.util.List<Long> serviceIds;
    
    // 如果是多人预约，可能需要 list of serviceId，这里先简化为单人单服务，或者聚合单服务

    /** 预约人数 (可选，默认为1) */
    private Integer peopleCount;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getTechId() {
        return techId;
    }

    public void setTechId(Long techId) {
        this.techId = techId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public java.util.List<Long> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(java.util.List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }
}
