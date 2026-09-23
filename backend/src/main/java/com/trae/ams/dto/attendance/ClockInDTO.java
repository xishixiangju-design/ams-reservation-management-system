package com.trae.ams.dto.attendance;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ClockInDTO {
    private Long storeId;
    
    private String type; // CLOCK_IN, CLOCK_OUT
    
    private String location;
    
    private LocalDateTime time; // Optional, defaults to now

    // Getters and Setters
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}
