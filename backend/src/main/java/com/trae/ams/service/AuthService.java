package com.trae.ams.service;

import com.trae.ams.dto.auth.LoginRequest;
import com.trae.ams.dto.auth.LoginResult;
import com.trae.ams.dto.auth.RegisterRequest;

public interface AuthService {
    /**
     * 用户登录
     */
    LoginResult login(LoginRequest request, String ipAddress);

    /**
     * 用户注册
     */
    void register(RegisterRequest request, String ipAddress);

    /**
     * 发送重置密码验证码
     */
    void sendResetCode(String email);

    /**
     * 重置密码
     */
    void resetPassword(String email, String code, String newPassword);

    /**
     * 用户登出
     */
    void logout();
}
