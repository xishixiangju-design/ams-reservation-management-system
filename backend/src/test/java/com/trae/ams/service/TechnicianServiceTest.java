package com.trae.ams.service;

import com.trae.ams.dto.technician.TechnicianDTO;
import com.trae.ams.entity.AmsTechnicianInfo;
import com.trae.ams.entity.SysRole;
import com.trae.ams.mapper.SysRoleMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class TechnicianServiceTest {

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @BeforeEach
    public void setup() {
        // 确保 ROLE_TECH 存在
        if (sysRoleMapper.selectByCode("ROLE_TECH") == null) {
            SysRole role = new SysRole();
            role.setCode("ROLE_TECH");
            role.setName("技师");
            sysRoleMapper.insert(role);
        }
    }

    @Test
    public void testTechnicianLifecycle() {
        // 1. Create
        TechnicianDTO dto = new TechnicianDTO();
        dto.setUsername("tech001");
        dto.setPassword("123456");
        dto.setNickname("Anna");
        dto.setRealName("Anna Li");
        dto.setLevel("Senior");
        dto.setIntroCn("擅长泰式按摩");

        technicianService.createTechnician(dto);

        // 2. List
        List<AmsTechnicianInfo> list = technicianService.listTechnicians(null, "Anna");
        Assertions.assertFalse(list.isEmpty());
        AmsTechnicianInfo tech = list.get(0);
        Assertions.assertEquals("Anna Li", tech.getRealName());
        Assertions.assertNotNull(tech.getSysUser());
        Assertions.assertEquals("tech001", tech.getSysUser().getUsername());

        // 3. Update Status
        technicianService.updateStatus(tech.getUserId(), "BUSY");
        AmsTechnicianInfo updated = technicianService.getTechnician(tech.getUserId());
        Assertions.assertEquals("BUSY", updated.getStatus());

        // 4. Update Info
        TechnicianDTO updateDto = new TechnicianDTO();
        updateDto.setRealName("Anna Lee");
        technicianService.updateTechnician(tech.getUserId(), updateDto);
        
        AmsTechnicianInfo updatedInfo = technicianService.getTechnician(tech.getUserId());
        Assertions.assertEquals("Anna Lee", updatedInfo.getRealName());
    }
}
