package com.trae.ams.entity;

import java.io.Serializable;

/**
 * 技师扩展信息表
 */
public class AmsTechnicianInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 关联 sys_user.id */
    private Long userId;

    /** 真实姓名 */
    private String realName;

    /** 职级 (Director/Senior) */
    private String level;

    /** IDLE(空闲), BUSY(忙碌), LEAVE(请假) */
    private String status;

    /** 轮牌当前顺位 (越小越优先) */
    private Integer wheelSeq;

    /** 中文简介 */
    private String introCn;

    /** 日文简介 */
    private String introJp;

    /** 上次服务结束时间 */
    private java.time.LocalDateTime lastJobEndTime;

    // 关联字段，非数据库字段
    private SysUser sysUser;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getWheelSeq() {
        return wheelSeq;
    }

    public void setWheelSeq(Integer wheelSeq) {
        this.wheelSeq = wheelSeq;
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

    public java.time.LocalDateTime getLastJobEndTime() {
        return lastJobEndTime;
    }

    public void setLastJobEndTime(java.time.LocalDateTime lastJobEndTime) {
        this.lastJobEndTime = lastJobEndTime;
    }

    public SysUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }
}
