package com.trae.ams.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 */
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属门店ID (超管为空) */
    private Long storeId;

    /** 登录账号/手机号 */
    private String username;

    /** MD5(pwd+salt) 密文 */
    private String password;

    /** 随机盐值 (16位+) */
    private String salt;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 1:正常, 0:禁用 */
    private Integer status;

    /** 会员等级 (NORMAL, GOLD, PLATINUM) */
    private String membershipLevel;

    /** 折扣率 (1.00, 0.90) */
    private java.math.BigDecimal discountRate;

    /** 违约次数 */
    private Integer violationCount;

    /** 累计消费金额 */
    private java.math.BigDecimal totalConsumption;

    /** 创建时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(String membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public java.math.BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(java.math.BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(Integer violationCount) {
        this.violationCount = violationCount;
    }

    public java.math.BigDecimal getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(java.math.BigDecimal totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
