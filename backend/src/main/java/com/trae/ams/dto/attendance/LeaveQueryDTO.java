package com.trae.ams.dto.attendance;

import java.time.LocalDateTime;

public class LeaveQueryDTO {
    private Long storeId;
    private Long techId;
    private String techName;
    private String type;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Getters and Setters
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public Long getTechId() { return techId; }
    public void setTechId(Long techId) { this.techId = techId; }
    public String getTechName() { return techName; }
    public void setTechName(String techName) { this.techName = techName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
