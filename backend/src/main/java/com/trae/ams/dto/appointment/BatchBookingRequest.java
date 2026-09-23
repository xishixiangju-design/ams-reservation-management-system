package com.trae.ams.dto.appointment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量预约请求 (Admin only)
 */
public class BatchBookingRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    private Long customerId;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 备注 */
    private String remark;

    /** 服务项目ID列表 (多选) */
    private List<Long> serviceIds;

    /** 预约开始时间列表 (多选) */
    private List<LocalDateTime> startTimes;

    /** 指定技师ID (可选，统一指定) */
    private Long techId;

    /** 店铺ID (可选) */
    private Long storeId;

    /** 创建人ID (可选) */
    private Long creatorId;

    /** 预约人数 (可选，默认为1) */
    private Integer peopleCount;

    /** 支付方式 (可选) */
    private String paymentMethod;

    /** 来源 (可选) */
    private String source;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Long> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public List<LocalDateTime> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(List<LocalDateTime> startTimes) {
        this.startTimes = startTimes;
    }

    public Long getTechId() {
        return techId;
    }

    public void setTechId(Long techId) {
        this.techId = techId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
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
