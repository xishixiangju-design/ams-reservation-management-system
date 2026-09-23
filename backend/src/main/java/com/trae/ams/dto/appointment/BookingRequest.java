package com.trae.ams.dto.appointment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约下单请求
 */
public class BookingRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 客户ID (如果是代客下单) */
    private Long customerId;

    /** 预约开始时间 */
    private LocalDateTime startTime;

    /** 备注 */
    private String remark;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 预约明细列表 */
    private List<BookingItemDTO> items;

    /** 预约状态 (可选，默认为5-待消费) */
    private Integer status;

    /** 店铺ID (可选，内部调用时指定) */
    private Long storeId;

    /** 预约人数 */
    private Integer peopleCount;

    /** 创建人ID (可选，内部调用时指定) */
    private Long creatorId;

    /** 支付方式 (可选，默认ALIPAY) */
    private String paymentMethod;

    /** 来源 (可选，默认CLIENT) */
    private String source;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public List<BookingItemDTO> getItems() {
        return items;
    }

    public void setItems(List<BookingItemDTO> items) {
        this.items = items;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
