package com.trae.ams.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SysNotification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long storeId;
    private Long userId;
    private String type; // EMAIL, SMS, SYSTEM
    private String title;
    private String content;
    private String target; // Email address or phone number
    private Integer status; // 0: Pending, 1: Sent, 2: Failed
    private Integer readStatus; // 0: Unread, 1: Read
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime sendTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getReadStatus() { return readStatus; }
    public void setReadStatus(Integer readStatus) { this.readStatus = readStatus; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
}
