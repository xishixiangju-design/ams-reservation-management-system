package com.trae.ams.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.common.exception.TechnicianScheduleConflictException;
import com.trae.ams.common.util.PasswordUtil;
import com.trae.ams.dto.technician.TechnicianConflictDTO;
import com.trae.ams.dto.technician.TechnicianDTO;
import com.trae.ams.dto.technician.TechnicianStatsDTO;
import com.trae.ams.entity.AmsAppointmentItem;
import com.trae.ams.entity.AmsTechnicianInfo;
import com.trae.ams.entity.SysUser;
import com.trae.ams.entity.SysUserRole;
import com.trae.ams.mapper.AmsAppointmentItemMapper;
import com.trae.ams.mapper.AmsTechnicianInfoMapper;
import com.trae.ams.mapper.SysRoleMapper;
import com.trae.ams.mapper.SysUserMapper;
import com.trae.ams.mapper.SysUserRoleMapper;
import com.trae.ams.service.TechnicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TechnicianServiceImpl implements TechnicianService {

    @Autowired
    private AmsTechnicianInfoMapper technicianInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired
    private SysRoleMapper sysRoleMapper; // 如果需要查询 RoleId，或者硬编码
    
    @Autowired
    private AmsAppointmentItemMapper appointmentItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTechnician(TechnicianDTO dto) {
        // 1. 检查用户名是否存在
        if (sysUserMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建 SysUser
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setStoreId(UserContext.getStoreIdOrDefault()); // 默认当前 StoreId
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());

        String salt = RandomUtil.randomString(16);
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(dto.getPassword(), salt));

        sysUserMapper.insert(user);

        // 3. 关联角色 (假设 ROLE_TECH 的 ID 是已知的，或者需要查询)
        Long roleId = sysRoleMapper.selectByCode("ROLE_TECH").getId();
        
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);

        // 4. 创建 AmsTechnicianInfo
        AmsTechnicianInfo info = new AmsTechnicianInfo();
        info.setUserId(user.getId());
        info.setRealName(dto.getRealName());
        info.setLevel(dto.getLevel());
        info.setStatus("IDLE"); // 默认空闲
        info.setWheelSeq(999); // 默认排在最后
        info.setIntroCn(dto.getIntroCn());
        info.setIntroJp(dto.getIntroJp());

        technicianInfoMapper.insert(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTechnician(Long userId, TechnicianDTO dto) {
        // 1. 更新 User 基本信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        boolean updateUser = false;
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            // Check username uniqueness
            SysUser existing = sysUserMapper.selectByUsername(dto.getUsername());
            if (existing != null && !existing.getId().equals(userId)) {
                throw new BusinessException("用户名/手机号已存在");
            }
            user.setUsername(dto.getUsername());
            updateUser = true;
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
            updateUser = true;
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
            updateUser = true;
        }
        if (dto.getUserStatus() != null) {
            user.setStatus(dto.getUserStatus());
            updateUser = true;
        }
        
        if (updateUser) {
            sysUserMapper.update(user);
        }

        // 2. 更新 TechnicianInfo
        AmsTechnicianInfo info = technicianInfoMapper.selectByUserId(userId);
        if (info == null) {
             throw new BusinessException("技师信息不存在");
        }
        
        AmsTechnicianInfo updateInfo = new AmsTechnicianInfo();
        updateInfo.setUserId(userId);
        updateInfo.setRealName(dto.getRealName());
        updateInfo.setLevel(dto.getLevel());
        updateInfo.setIntroCn(dto.getIntroCn());
        updateInfo.setIntroJp(dto.getIntroJp());
        
        technicianInfoMapper.update(updateInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, String status) {
        if ("LEAVE".equals(status)) {
            // Check for future appointments
            List<TechnicianConflictDTO> conflicts = appointmentItemMapper.selectConflictDetailsByTechId(userId, LocalDateTime.now());
            if (CollUtil.isNotEmpty(conflicts)) {
                throw new TechnicianScheduleConflictException(conflicts);
            }
        }
        
        AmsTechnicianInfo info = new AmsTechnicianInfo();
        info.setUserId(userId);
        info.setStatus(status);
        technicianInfoMapper.update(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTechnician(Long userId) {
        // Check for active orders
        int activeOrders = appointmentItemMapper.countActiveByTechId(userId);
        if (activeOrders > 0) {
            throw new BusinessException("该技师有进行中的订单，无法删除，请先停用");
        }

        // Delete related data
        sysUserRoleMapper.deleteByUserId(userId);
        technicianInfoMapper.deleteByUserId(userId);
        sysUserMapper.deleteById(userId);
    }

    @Override
    public TechnicianStatsDTO getTechnicianStats(Long userId) {
        AmsTechnicianInfo info = technicianInfoMapper.selectByUserId(userId);
        if (info == null) {
            throw new BusinessException("技师不存在");
        }
        
        TechnicianStatsDTO stats = new TechnicianStatsDTO();
        stats.setUserId(userId);
        stats.setTechnicianName(info.getRealName());
        stats.setActiveOrderCount(appointmentItemMapper.countActiveByTechId(userId));
        stats.setTotalOrderCount(appointmentItemMapper.countTotalByTechId(userId));
        
        return stats;
    }

    @Override
    public AmsTechnicianInfo getTechnician(Long userId) {
        return technicianInfoMapper.selectByUserId(userId);
    }

    @Override
    public List<AmsTechnicianInfo> listTechnicians(String status, String name) {
        return technicianInfoMapper.selectList(UserContext.getStoreIdOrDefault(), status, name);
    }
}
