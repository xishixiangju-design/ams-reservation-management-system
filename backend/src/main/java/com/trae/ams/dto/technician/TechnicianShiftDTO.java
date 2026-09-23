package com.trae.ams.dto.technician;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 技师排班 DTO
 */
public class TechnicianShiftDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long techId;
    private String techName; // 用于展示
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String type; // WORK, LEAVE, BREAK

    // 批量设置用
    private LocalDate startDate;
    private LocalDate endDate;
    
    // 周几 (1-7)
    private Integer dayOfWeek; 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTechId() {
        return techId;
    }

    public void setTechId(Long techId) {
        this.techId = techId;
    }

    public String getTechName() {
        return techName;
    }

    public void setTechName(String techName) {
        this.techName = techName;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
