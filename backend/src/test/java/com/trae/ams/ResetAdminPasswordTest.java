package com.trae.ams;

import com.trae.ams.common.util.PasswordUtil;
import com.trae.ams.entity.SysRole;
import com.trae.ams.entity.SysUser;
import com.trae.ams.entity.SysUserRole;
import com.trae.ams.mapper.SysRoleMapper;
import com.trae.ams.mapper.SysUserMapper;
import com.trae.ams.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ResetAdminPasswordTest {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Test
    public void resetPassword() {
        String username = "admin";
        String password = "admin123";
        
        // 1. Handle User
        SysUser user = sysUserMapper.selectByUsername(username);
        
        String salt = PasswordUtil.generateSalt();
        String encrypted = PasswordUtil.encrypt(password, salt);
        
        if (user == null) {
            System.out.println("Creating new admin user...");
            user = new SysUser();
            user.setUsername(username);
            user.setNickname("Super Admin");
            user.setStatus(1);
            user.setSalt(salt);
            user.setPassword(encrypted);
            sysUserMapper.insert(user);
        } else {
            System.out.println("Updating existing admin user...");
            user.setSalt(salt);
            user.setPassword(encrypted);
            user.setStatus(1); // Ensure it's active
            sysUserMapper.update(user);
        }

        // 2. Handle Role
        String roleCode = "ROLE_ADMIN";
        SysRole role = sysRoleMapper.selectByCode(roleCode);
        if (role == null) {
            System.out.println("Creating ROLE_ADMIN...");
            role = new SysRole();
            role.setCode(roleCode);
            role.setName("Administrator");
            sysRoleMapper.insert(role);
        }

        // 3. Handle User-Role Relation
        List<String> roles = sysUserRoleMapper.selectRoleCodesByUserId(user.getId());
        if (!roles.contains(roleCode)) {
            System.out.println("Assigning ROLE_ADMIN to user...");
            sysUserRoleMapper.insert(new SysUserRole(user.getId(), role.getId()));
        } else {
            System.out.println("User already has ROLE_ADMIN.");
        }
        
        System.out.println("=========================================");
        System.out.println("Admin Password Reset Successfully!");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Salt: " + salt);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("=========================================");
    }
}
