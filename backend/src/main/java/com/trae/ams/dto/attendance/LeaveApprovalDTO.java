package com.trae.ams.dto.attendance;

import jakarta.validation.constraints.NotNull;

public class LeaveApprovalDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    
    @NotNull(message = "状态不能为空")
    private String status; // APPROVED, REJECTED

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
