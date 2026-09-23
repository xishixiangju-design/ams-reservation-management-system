package com.trae.ams.service;

import com.trae.ams.dto.technician.TechnicianShiftDTO;
import com.trae.ams.entity.AmsTechnicianShift;

import java.time.LocalDate;
import java.util.List;

public interface TechnicianShiftService {
    
    /**
     * 添加或更新单条排班
     */
    void saveShift(TechnicianShiftDTO dto);

    /**
     * 批量排班 (按日期范围)
     */
    void batchSchedule(TechnicianShiftDTO dto);

    /**
     * 删除排班
     */
    void deleteShift(Long id);

    /**
     * 获取指定技师某天的排班
     */
    List<AmsTechnicianShift> getShifts(Long techId, LocalDate date);

    /**
     * 获取某天所有技师的排班
     */
    List<AmsTechnicianShift> getAllShiftsByDate(LocalDate date);
    
    /**
     * 检查技师在指定时间段是否工作 (且未请假)
     */
    boolean isTechWorking(Long techId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
