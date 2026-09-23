package com.trae.ams.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员扩展表
 */
public class MemberInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 关联 sys_user.id */
    private Long userId;

    /** 所属门店ID (0为通用) */
    private Long storeId;

    /** 会员等级 */
    private String level;

    /** 余额 */
    private BigDecimal balance;

    /** 当月违约次数 */
    private Integer violationCountMonth;

    /** 预约限制结束时间 */
    private LocalDateTime restrictionEndTime;

    /** 过敏史 */
    private String allergyHistory;

    /** 服务偏好 */
    private String servicePreference;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getViolationCountMonth() {
        return violationCountMonth;
    }

    public void setViolationCountMonth(Integer violationCountMonth) {
        this.violationCountMonth = violationCountMonth;
    }

    public LocalDateTime getRestrictionEndTime() {
        return restrictionEndTime;
    }

    public void setRestrictionEndTime(LocalDateTime restrictionEndTime) {
        this.restrictionEndTime = restrictionEndTime;
    }

    public String getAllergyHistory() {
        return allergyHistory;
    }

    public void setAllergyHistory(String allergyHistory) {
        this.allergyHistory = allergyHistory;
    }

    public String getServicePreference() {
        return servicePreference;
    }

    public void setServicePreference(String servicePreference) {
        this.servicePreference = servicePreference;
    }
}
