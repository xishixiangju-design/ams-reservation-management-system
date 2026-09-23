package com.trae.ams.dto.appointment;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * 可用时间槽结果
 */
public class TimeSlotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 是否可用 */
    private boolean available;

    /** 剩余可用技师数 (可选) */
    private int availableTechCount;

    public TimeSlotDTO() {}

    public TimeSlotDTO(LocalTime startTime, LocalTime endTime, boolean available) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getAvailableTechCount() {
        return availableTechCount;
    }

    public void setAvailableTechCount(int availableTechCount) {
        this.availableTechCount = availableTechCount;
    }
}
