package com.trae.ams.controller;

import com.trae.ams.common.result.Result;
import com.trae.ams.dto.technician.TechnicianDTO;
import com.trae.ams.dto.technician.TechnicianStatsDTO;
import com.trae.ams.entity.AmsTechnicianInfo;
import com.trae.ams.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/technicians")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

    @PostMapping
    public Result<Void> create(@RequestBody TechnicianDTO dto) {
        technicianService.createTechnician(dto);
        return Result.success();
    }

    @PutMapping("/{userId}")
    public Result<Void> update(@PathVariable Long userId, @RequestBody TechnicianDTO dto) {
        technicianService.updateTechnician(userId, dto);
        return Result.success();
    }
    
    @PatchMapping("/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestParam String status) {
        technicianService.updateStatus(userId, status);
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable Long userId) {
        technicianService.deleteTechnician(userId);
        return Result.success();
    }

    @GetMapping("/{userId}/stats")
    public Result<TechnicianStatsDTO> getStats(@PathVariable Long userId) {
        return Result.success(technicianService.getTechnicianStats(userId));
    }

    @GetMapping("/{userId}")
    public Result<AmsTechnicianInfo> get(@PathVariable Long userId) {
        return Result.success(technicianService.getTechnician(userId));
    }

    @GetMapping
    public Result<List<AmsTechnicianInfo>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name) {
        return Result.success(technicianService.listTechnicians(status, name));
    }
}
