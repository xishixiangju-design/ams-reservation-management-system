package com.trae.ams.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 违约记录表
 */
public class AmsViolationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属门店ID */
    private Long storeId;

    /** 关联用户ID */
    private Long userId;

    /** 关联预约ID */
    private Long apptId;

    /** LATE_CANCEL(超时取消), NO_SHOW(爽约) */
    private String type;

    /** 违约发生时间 */
    private LocalDateTime violationTime;

    /** 记录创建时间 */
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getApptId() {
        return apptId;
    }

    public void setApptId(Long apptId) {
        this.apptId = apptId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getViolationTime() {
        return violationTime;
    }

    public void setViolationTime(LocalDateTime violationTime) {
        this.violationTime = violationTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
