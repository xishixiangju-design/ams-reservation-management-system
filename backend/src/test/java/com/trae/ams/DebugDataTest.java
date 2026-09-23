package com.trae.ams;

import com.trae.ams.entity.AmsAppointment;
import com.trae.ams.entity.SysUser;
import com.trae.ams.mapper.AmsAppointmentMapper;
import com.trae.ams.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DebugDataTest {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private AmsAppointmentMapper appointmentMapper;

    @Test
    public void dumpData() {
        System.out.println("=== DEBUG DATA DUMP START ===");
        
        System.out.println("--- Users ---");
        List<SysUser> users = userMapper.selectList(null); // Assuming selectList exists or similar
        // If selectList(null) is not available, try other method or just check mapper
        // Based on previous search, SysUserMapper has selectById. Let's assume standard MyBatis methods or check XML.
        // Wait, I haven't seen SysUserMapper.xml fully. 
        // Let's try to query by ID 1 which is usually admin/default.
        SysUser u1 = userMapper.selectById(1L);
        System.out.println("User 1: " + (u1 != null ? u1.getUsername() : "null"));
        
        System.out.println("--- Appointments ---");
        // Use the admin list method to get all
        List<AmsAppointment> appts = appointmentMapper.selectList(null, null, null, null, null);
        for (AmsAppointment a : appts) {
            System.out.println(String.format("Appt ID: %d, CustomerID: %d, Status: %d, Time: %s", 
                a.getId(), a.getCustomerId(), a.getStatus(), a.getStartTime()));
        }
        
        System.out.println("=== DEBUG DATA DUMP END ===");
    }
}
