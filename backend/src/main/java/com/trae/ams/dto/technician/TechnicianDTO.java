package com.trae.ams.dto.technician;

import java.io.Serializable;

/**
 * 技师信息 DTO
 */
public class TechnicianDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 用户基本信息
    private String username;
    private String password; // 仅新增时需要
    private String nickname;
    private String avatar;
    private Integer userStatus; // 1:正常, 0:禁用

    // 技师扩展信息
    private String realName;
    private String level;
    private String status; // IDLE, BUSY, LEAVE
    private String introCn;
    private String introJp;

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntroCn() {
        return introCn;
    }

    public void setIntroCn(String introCn) {
        this.introCn = introCn;
    }

    public String getIntroJp() {
        return introJp;
    }

    public void setIntroJp(String introJp) {
        this.introJp = introJp;
    }
}
