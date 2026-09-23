package com.trae.ams.service;

import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.common.util.PasswordUtil;
import com.trae.ams.dto.auth.LoginRequest;
import com.trae.ams.dto.auth.LoginResult;
import com.trae.ams.dto.auth.RegisterRequest;
import com.trae.ams.entity.MemberInfo;
import com.trae.ams.entity.SysRole;
import com.trae.ams.entity.SysUser;
import com.trae.ams.entity.SysUserRole;
import com.trae.ams.mapper.MemberInfoMapper;
import com.trae.ams.mapper.SysRoleMapper;
import com.trae.ams.mapper.SysUserMapper;
import com.trae.ams.mapper.SysUserRoleMapper;
import com.trae.ams.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private MemberInfoMapper memberInfoMapper;

    @Mock
    private com.trae.ams.common.cache.LocalCache localCache;

    @Mock
    private com.trae.ams.common.util.JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser mockUser;

    @BeforeEach
    void setUp() {
        String salt = PasswordUtil.generateSalt();
        mockUser = new SysUser();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setSalt(salt);
        mockUser.setPassword(PasswordUtil.encrypt("password123", salt));
        mockUser.setStatus(1);
        mockUser.setNickname("Test User");
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");
        req.setCaptchaUuid("uuid");
        req.setUserInputCaptcha("1234");

        when(localCache.get("captcha:uuid")).thenReturn("1234");
        when(sysUserMapper.selectByUsername("testuser")).thenReturn(mockUser);
        when(sysUserRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(Collections.singletonList("ROLE_CLIENT"));
        when(jwtUtil.createToken(anyLong(), anyString())).thenReturn("mock-token");

        LoginResult result = authService.login(req, "127.0.0.1");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLoginIpLockPwd() {
        // Mock IP lock check: password failure count >= 5
        when(localCache.get("ip_fail_pwd:127.0.0.1")).thenReturn("5");

        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(req, "127.0.0.1"));
        assertEquals("账号密码错误次数过多，IP已被锁定30分钟", exception.getMessage());
    }

    @Test
    void testLoginIpLockCaptcha() {
        // Mock IP lock check: captcha failure count >= 10
        when(localCache.get("ip_fail_pwd:127.0.0.1")).thenReturn(null);
        when(localCache.get("ip_fail_captcha:127.0.0.1")).thenReturn("10");

        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(req, "127.0.0.1"));
        assertEquals("验证码错误次数过多，IP已被锁定30分钟", exception.getMessage());
    }

    @Test
    void testLoginPasswordFailIncrementsCount() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("wrongpass");
        req.setCaptchaUuid("uuid");
        req.setUserInputCaptcha("1234");

        // Mock captcha validation success
        when(localCache.get("captcha:uuid")).thenReturn("1234");
        
        // Mock user found
        when(sysUserMapper.selectByUsername("testuser")).thenReturn(mockUser);
        
        // Mock password fail handling
        when(localCache.increment("ip_fail_pwd:127.0.0.1")).thenReturn(1L);

        assertThrows(BusinessException.class, () -> authService.login(req, "127.0.0.1"));
        
        // Verify increment called
        verify(localCache).increment("ip_fail_pwd:127.0.0.1");
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password123");
        req.setNickname("New User");
        req.setCaptchaUuid("uuid");
        req.setUserInputCaptcha("1234");

        when(localCache.get("captcha:uuid")).thenReturn("1234");
        when(sysUserMapper.selectByUsername("newuser")).thenReturn(null);
        
        SysRole clientRole = new SysRole();
        clientRole.setId(2L);
        clientRole.setCode("ROLE_CLIENT");
        when(sysRoleMapper.selectByCode("ROLE_CLIENT")).thenReturn(clientRole);

        authService.register(req, "127.0.0.1");

        verify(sysUserMapper).insert(any(SysUser.class));
        verify(sysUserRoleMapper).insert(any(SysUserRole.class));
        verify(memberInfoMapper).insert(any(MemberInfo.class));
    }

    @Test
    void testRegisterUserExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setCaptchaUuid("uuid");
        req.setUserInputCaptcha("1234");

        when(localCache.get("captcha:uuid")).thenReturn("1234");
        when(sysUserMapper.selectByUsername("testuser")).thenReturn(mockUser);

        assertThrows(BusinessException.class, () -> authService.register(req, "127.0.0.1"));
    }
}
