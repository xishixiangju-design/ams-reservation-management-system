package com.trae.ams.service.impl;

import cn.hutool.core.util.StrUtil;
import com.trae.ams.common.cache.LocalCache;
import com.trae.ams.common.exception.BusinessException;
import com.trae.ams.common.util.JwtUtil;
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
import com.trae.ams.service.AuthService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private MemberInfoMapper memberInfoMapper;

    @Resource
    private LocalCache localCache;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public LoginResult login(LoginRequest request, String ipAddress) {
        // 1. Check IP Lock
        checkIpLock(ipAddress);

        // 2. Validate Captcha
        validateCaptcha(request.getCaptchaUuid(), request.getUserInputCaptcha(), ipAddress);

        // 3. Query User
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            handlePwdFail(ipAddress);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 4. Verify Password
        if (!PasswordUtil.verify(request.getPassword(), user.getSalt(), user.getPassword())) {
            handlePwdFail(ipAddress);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 5. Check Status
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 6. Get Roles
        List<String> roles = sysUserRoleMapper.selectRoleCodesByUserId(user.getId());

        // 7. Issue JWT
        String token = jwtUtil.createToken(user.getId(), user.getUsername());
        
        // Log successful login (Audit)
        log.info("User logged in: id={}, username={}, roles={}", user.getId(), user.getUsername(), roles);

        // 8. Return Result
        return new LoginResult(user.getId(), user.getUsername(), user.getNickname(), roles, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request, String ipAddress) {
        // 1. Check IP Lock
        checkIpLock(ipAddress);

        // 2. Validate Captcha
        validateCaptcha(request.getCaptchaUuid(), request.getUserInputCaptcha(), ipAddress);

        // 3. Check Duplicate
        SysUser existingUser = sysUserMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(409, "用户名已存在");
        }
        
        SysUser existingEmail = sysUserMapper.selectByEmail(request.getEmail());
        if (existingEmail != null) {
            throw new BusinessException(409, "该邮箱已被注册");
        }

        // 4. Create User
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encrypt(request.getPassword(), salt);

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 5. Bind Default Role (ROLE_CUSTOMER)
        SysRole clientRole = sysRoleMapper.selectByCode("ROLE_CUSTOMER");
        if (clientRole == null) {
            throw new BusinessException(500, "系统配置错误：默认角色 ROLE_CUSTOMER 不存在");
        }
        sysUserRoleMapper.insert(new SysUserRole(user.getId(), clientRole.getId()));

        // 6. Initialize Member Info
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setUserId(user.getId());
        memberInfo.setStoreId(0L); // Default
        memberInfo.setLevel("SILVER");
        memberInfo.setBalance(BigDecimal.ZERO);
        memberInfo.setViolationCountMonth(0);
        memberInfoMapper.insert(memberInfo);
    }

    @Override
    public void sendResetCode(String email) {
        try {
            log.info("Request to send reset code to: {}", email);
            
            // Check if user exists
            SysUser user = sysUserMapper.selectByEmail(email);
            if (user == null) {
                // To prevent enumeration attacks, we might not want to reveal this.
                // But for now, let's be explicit.
                throw new BusinessException("该邮箱未注册");
            }

            // Generate Code
            String code = cn.hutool.core.util.RandomUtil.randomNumbers(6);
            String key = "reset_code:" + email;

            // Store in cache (5 mins)
            localCache.set(key, code, 5, TimeUnit.MINUTES);

            // Send Email
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【AMS】重置密码验证码");
            message.setText("您的验证码是：" + code + "，有效期5分钟。请勿泄露给他人。");
            
            mailSender.send(message);
            log.info("Reset code sent to {}", email);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send reset code email to {}", email, e);
            // Return specific error info to help debugging
            Throwable cause = e.getCause();
            String msg = (cause != null) ? cause.getMessage() : e.getMessage();
            throw new BusinessException("发送验证码失败: " + e.getClass().getSimpleName() + ": " + msg);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String code, String newPassword) {
        String key = "reset_code:" + email;
        String correctCode = localCache.get(key);

        if (correctCode == null || !correctCode.equals(code)) {
            throw new BusinessException("验证码无效或已过期");
        }

        SysUser user = sysUserMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // Reset Password
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encrypt(newPassword, salt);
        
        user.setSalt(salt);
        user.setPassword(encryptedPassword);
        
        sysUserMapper.update(user);
        
        // Remove code
        localCache.delete(key);
    }

    @Override
    public void logout() {
        // JWT is stateless
    }

    private static final String PWD_FAIL_KEY = "ip_fail_pwd:";
    private static final String CAPTCHA_FAIL_KEY = "ip_fail_captcha:";

    private void checkIpLock(String ip) {
        String pwdKey = PWD_FAIL_KEY + ip;
        String pwdVal = localCache.get(pwdKey);
        if (pwdVal != null && Integer.parseInt(pwdVal) >= 5) {
            throw new BusinessException(403, "账号密码错误次数过多，IP已被锁定30分钟");
        }

        String captchaKey = CAPTCHA_FAIL_KEY + ip;
        String captchaVal = localCache.get(captchaKey);
        if (captchaVal != null && Integer.parseInt(captchaVal) >= 10) {
            throw new BusinessException(403, "验证码错误次数过多，IP已被锁定30分钟");
        }
    }

    private void validateCaptcha(String uuid, String userInput, String ip) {
        if (StrUtil.isBlank(uuid) || StrUtil.isBlank(userInput)) {
             throw new BusinessException(40001, "验证码错误");
        }

        String key = "captcha:" + uuid;
        String correctCode = localCache.get(key);
        
        // Always delete captcha after use
        localCache.delete(key);

        if (correctCode == null || !correctCode.equalsIgnoreCase(userInput)) {
            handleCaptchaFail(ip);
            throw new BusinessException(40001, "验证码错误");
        }
    }

    private void handlePwdFail(String ip) {
        String key = PWD_FAIL_KEY + ip;
        Long count = localCache.increment(key);
        if (count != null && count == 1) {
            localCache.expire(key, 10, TimeUnit.MINUTES);
        }
        if (count != null && count >= 5) {
            localCache.expire(key, 30, TimeUnit.MINUTES);
        }
    }

    private void handleCaptchaFail(String ip) {
        String key = CAPTCHA_FAIL_KEY + ip;
        Long count = localCache.increment(key);
        if (count != null && count == 1) {
            localCache.expire(key, 10, TimeUnit.MINUTES);
        }
        if (count != null && count >= 10) {
            localCache.expire(key, 30, TimeUnit.MINUTES);
        }
    }
}
