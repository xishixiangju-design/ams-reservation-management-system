package com.trae.ams.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技师考勤打卡表
 */
public class AmsAttendance implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联店铺ID */
    private Long storeId;

    /** 技师ID (sys_user.id) */
    private Long techId;

    /** 类型: CLOCK_IN(上班打卡), CLOCK_OUT(下班打卡) */
    private String type;

    /** 打卡时间 */
    private LocalDateTime time;

    /** 打卡地点 */
    private String location;

    /** 打卡状态: NORMAL(正常), LATE(迟到), EARLY_LEAVE(早退), MISSING(缺卡) */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;
    
    // 非数据库字段
    private String techName;

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

    public Long getTechId() {
        return techId;
    }

    public void setTechId(Long techId) {
        this.techId = techId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getTechName() {
        return techName;
    }

    public void setTechName(String techName) {
        this.techName = techName;
    }
}
