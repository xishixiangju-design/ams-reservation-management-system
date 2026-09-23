package com.trae.ams.dto.technician;

import java.io.Serializable;

/**
 * 技师统计信息 DTO
 */
public class TechnicianStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String technicianName;
    private Integer activeOrderCount; // 进行中的订单数
    private Integer totalOrderCount;  // 历史总订单数

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public Integer getActiveOrderCount() {
        return activeOrderCount;
    }

    public void setActiveOrderCount(Integer activeOrderCount) {
        this.activeOrderCount = activeOrderCount;
    }

    public Integer getTotalOrderCount() {
        return totalOrderCount;
    }

    public void setTotalOrderCount(Integer totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }
}
