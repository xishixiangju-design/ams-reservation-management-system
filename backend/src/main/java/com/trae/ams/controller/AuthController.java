package com.trae.ams.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.trae.ams.common.result.Result;
import com.trae.ams.dto.auth.LoginRequest;
import com.trae.ams.dto.auth.LoginResult;
import com.trae.ams.dto.auth.RegisterRequest;
import com.trae.ams.dto.auth.ResetPasswordRequest;
import com.trae.ams.dto.auth.SendCodeRequest;
import com.trae.ams.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String ip = JakartaServletUtil.getClientIP(servletRequest);
        LoginResult result = authService.login(request, ip);
        return Result.success(result);
    }

    @PostMapping("/send-reset-code")
    public Result<Void> sendResetCode(@RequestBody @Valid SendCodeRequest request) {
        authService.sendResetCode(request.getEmail());
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        String ip = JakartaServletUtil.getClientIP(servletRequest);
        authService.register(request, ip);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
