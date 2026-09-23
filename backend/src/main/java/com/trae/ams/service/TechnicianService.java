package com.trae.ams.service;

import com.trae.ams.dto.technician.TechnicianDTO;
import com.trae.ams.dto.technician.TechnicianStatsDTO;
import com.trae.ams.entity.AmsTechnicianInfo;

import java.util.List;

public interface TechnicianService {
    /**
     * 创建技师 (包含创建 User 和 TechInfo)
     */
    void createTechnician(TechnicianDTO dto);

    /**
     * 更新技师信息
     */
    void updateTechnician(Long userId, TechnicianDTO dto);

    /**
     * 更新技师状态 (IDLE/BUSY/LEAVE)
     */
    void updateStatus(Long userId, String status);

    void deleteTechnician(Long userId);

    TechnicianStatsDTO getTechnicianStats(Long userId);

    /**
     * 获取技师详情
     */
    AmsTechnicianInfo getTechnician(Long userId);

    /**
     * 获取技师列表
     */
    List<AmsTechnicianInfo> listTechnicians(String status, String name);
}
