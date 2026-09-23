package com.trae.ams.dto.auth;

import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String captchaUuid;
    private String userInputCaptcha;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCaptchaUuid() {
        return captchaUuid;
    }

    public void setCaptchaUuid(String captchaUuid) {
        this.captchaUuid = captchaUuid;
    }

    public String getUserInputCaptcha() {
        return userInputCaptcha;
    }

    public void setUserInputCaptcha(String userInputCaptcha) {
        this.userInputCaptcha = userInputCaptcha;
    }
}
