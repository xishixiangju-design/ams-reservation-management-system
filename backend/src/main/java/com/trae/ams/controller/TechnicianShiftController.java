package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import com.trae.ams.dto.technician.TechnicianShiftDTO;
import com.trae.ams.entity.AmsTechnicianShift;
import com.trae.ams.service.TechnicianShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/shifts")
public class TechnicianShiftController {

    @Autowired
    private TechnicianShiftService shiftService;

    @PostMapping
    public Result<Void> save(@RequestBody TechnicianShiftDTO dto) {
        shiftService.saveShift(dto);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchSchedule(@RequestBody TechnicianShiftDTO dto) {
        shiftService.batchSchedule(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return Result.success();
    }

    @GetMapping
    public Result<List<AmsTechnicianShift>> getShifts(
            @RequestParam Long techId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(shiftService.getShifts(techId, date));
    }

    @GetMapping("/day")
    public Result<List<AmsTechnicianShift>> getAllShifts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(shiftService.getAllShiftsByDate(date));
    }
}
