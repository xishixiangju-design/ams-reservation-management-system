package com.trae.ams.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AmsMemberLevelLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String oldLevel;
    private String newLevel;
    private BigDecimal triggerAmount;
    private BigDecimal totalConsumption;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOldLevel() { return oldLevel; }
    public void setOldLevel(String oldLevel) { this.oldLevel = oldLevel; }

    public String getNewLevel() { return newLevel; }
    public void setNewLevel(String newLevel) { this.newLevel = newLevel; }

    public BigDecimal getTriggerAmount() { return triggerAmount; }
    public void setTriggerAmount(BigDecimal triggerAmount) { this.triggerAmount = triggerAmount; }

    public BigDecimal getTotalConsumption() { return totalConsumption; }
    public void setTotalConsumption(BigDecimal totalConsumption) { this.totalConsumption = totalConsumption; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
