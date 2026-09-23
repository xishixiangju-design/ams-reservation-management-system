package com.trae.ams.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约订单主表
 */
public class AmsAppointment implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属门店ID */
    private Long storeId;

    /** 客户ID */
    private Long customerId;

    /** 创建人ID */
    private Long creatorId;

    /** 预约开始时间 (冗余/聚合) */
    private LocalDateTime startTime;

    /** 预约结束时间 (冗余/聚合) */
    private LocalDateTime endTime;

    /** 总人数 */
    private Integer peopleCount;

    /** 2:已完成, 3:已取消, 4:违约, 5:待消费 */
    private Integer status;

    // REMOVED: STATUS_PENDING_CONFIRMATION = 0
    public static final int STATUS_PENDING_PAYMENT = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_CANCELLED = 3;
    public static final int STATUS_VIOLATION = 4;
    public static final int STATUS_PENDING_CONSUMPTION = 5;

    /** 支付状态: UNPAID, PAID, REFUNDED */
    private String paymentStatus;

    /** 退款状态: 0-未退款，1-退款中，2-已退款，3-退款失败 */
    private Integer refundStatus;

    public static final int REFUND_STATUS_NONE = 0;
    public static final int REFUND_STATUS_PROCESSING = 1;
    public static final int REFUND_STATUS_COMPLETED = 2;
    public static final int REFUND_STATUS_FAILED = 3;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 备注 */
    private String remark;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 下单时间 */
    private LocalDateTime createTime;

    /** 支付方式: ALIPAY, OFFLINE */
    private String paymentMethod;

    /** 来源: CLIENT, ADMIN */
    private String source;

    // 关联字段
    private SysUser customer;
    
    private java.util.List<AmsAppointmentItem> items;

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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(Integer refundStatus) {
        this.refundStatus = refundStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

    public SysUser getCustomer() {
        return customer;
    }

    public void setCustomer(SysUser customer) {
        this.customer = customer;
    }

    public java.util.List<AmsAppointmentItem> getItems() {
        return items;
    }

    public void setItems(java.util.List<AmsAppointmentItem> items) {
        this.items = items;
    }

    /**
     * 核销码 (虚拟字段，不存库)
     * 格式: V + 门店ID(3位补0) + 订单ID(8位补0)
     * 仅在待消费状态下有效
     */
    public String getVerificationCode() {
        if (this.id == null) {
            return null;
        }
        // 仅在待消费状态 (5) 显示核销码
        if (this.status != null && this.status == STATUS_PENDING_CONSUMPTION) {
            long sId = this.storeId != null ? this.storeId : 0;
            return String.format("V%03d%08d", sId % 1000, this.id % 100000000);
        }
        return null;
    }
}
