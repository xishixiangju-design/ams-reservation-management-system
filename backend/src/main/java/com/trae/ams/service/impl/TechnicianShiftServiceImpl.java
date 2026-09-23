package com.trae.ams.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.trae.ams.dto.technician.TechnicianShiftDTO;
import com.trae.ams.entity.AmsTechnicianShift;
import com.trae.ams.entity.AmsTechnicianInfo;
import com.trae.ams.mapper.AmsTechnicianShiftMapper;
import com.trae.ams.mapper.AmsTechnicianInfoMapper;
import com.trae.ams.service.TechnicianShiftService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TechnicianShiftServiceImpl implements TechnicianShiftService {

    @Autowired
    private AmsTechnicianShiftMapper shiftMapper;
    
    @Autowired
    private AmsTechnicianInfoMapper techInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShift(TechnicianShiftDTO dto) {
        if (dto.getId() != null) {
            AmsTechnicianShift shift = new AmsTechnicianShift();
            BeanUtils.copyProperties(dto, shift);
            shiftMapper.update(shift);
        } else {
            AmsTechnicianShift shift = new AmsTechnicianShift();
            BeanUtils.copyProperties(dto, shift);
            shiftMapper.insert(shift);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSchedule(TechnicianShiftDTO dto) {
        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();
        
        if (start == null || end == null || start.isAfter(end)) {
            throw new RuntimeException("Invalid date range");
        }
        
        // Loop through dates
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            // Check day of week if specified
            if (dto.getDayOfWeek() != null && curr.getDayOfWeek().getValue() != dto.getDayOfWeek()) {
                curr = curr.plusDays(1);
                continue;
            }
            
            // Delete existing shifts for this tech on this day (Override mode)
            shiftMapper.deleteByTechAndDate(dto.getTechId(), curr);
            
            // Insert new shift
            AmsTechnicianShift shift = new AmsTechnicianShift();
            shift.setTechId(dto.getTechId());
            shift.setShiftDate(curr);
            shift.setStartTime(dto.getStartTime());
            shift.setEndTime(dto.getEndTime());
            shift.setType(dto.getType() != null ? dto.getType() : "WORK");
            
            shiftMapper.insert(shift);
            
            curr = curr.plusDays(1);
        }
    }

    @Override
    public void deleteShift(Long id) {
        shiftMapper.deleteById(id);
    }

    @Override
    public List<AmsTechnicianShift> getShifts(Long techId, LocalDate date) {
        return shiftMapper.selectByTechAndDate(techId, date);
    }

    @Override
    public List<AmsTechnicianShift> getAllShiftsByDate(LocalDate date) {
        return shiftMapper.selectAllByDate(date);
    }

    @Override
    public boolean isTechWorking(Long techId, LocalDateTime start, LocalDateTime end) {
        LocalDate date = start.toLocalDate();
        
        // 1. Get shifts for the day
        List<AmsTechnicianShift> shifts = shiftMapper.selectByTechAndDate(techId, date);
        
        // 2. Fallback to legacy check if no shifts defined
        if (CollUtil.isEmpty(shifts)) {
            // Fallback: Check global status. If LEAVE, then not working. Otherwise assume working.
            // Note: Ideally we should default to Store Hours if no shift is present, 
            // but for backward compatibility, we assume available unless explicit LEAVE status in TechInfo.
            AmsTechnicianInfo info = techInfoMapper.selectByUserId(techId);
            return info != null && !"LEAVE".equals(info.getStatus());
        }
        
        // 3. Check coverage
        LocalTime timeStart = start.toLocalTime();
        LocalTime timeEnd = end.toLocalTime();
        
        boolean covered = false;
        boolean blocked = false;
        
        for (AmsTechnicianShift shift : shifts) {
            // Check for Blocking shifts (LEAVE, BREAK)
            if (!"WORK".equals(shift.getType())) {
                if (isOverlap(timeStart, timeEnd, shift.getStartTime(), shift.getEndTime())) {
                    blocked = true;
                    break;
                }
            } else {
                // Check for Coverage by WORK shift
                // We need the WORK shift to cover the ENTIRE requested period.
                // Assuming simple case: one work shift covers it. 
                // If multiple split shifts (e.g. 10-12, 13-18) and request is 11-14, it fails.
                if (shift.getStartTime().compareTo(timeStart) <= 0 && shift.getEndTime().compareTo(timeEnd) >= 0) {
                    covered = true;
                }
            }
        }
        
        return covered && !blocked;
    }
    
    private boolean isOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
