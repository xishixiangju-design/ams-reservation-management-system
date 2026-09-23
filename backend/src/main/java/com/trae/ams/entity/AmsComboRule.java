package com.trae.ams.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐组合优惠规则表
 */
public class AmsComboRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /** 所属门店ID */
    private Long storeId;

    /** 规则名称 */
    private String name;

    /** 条件JSON (例如: {"requiredServiceIds": [1, 2]}) */
    private String conditionJson;

    /** 折扣类型 (1: 折扣率, 2: 固定减免金额) */
    private Integer discountType;

    /** 折扣值 (如 0.9 或 50.00) */
    private BigDecimal discountValue;

    /** 1:生效, 0:失效 */
    private Integer status;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConditionJson() {
        return conditionJson;
    }

    public void setConditionJson(String conditionJson) {
        this.conditionJson = conditionJson;
    }

    public Integer getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Integer discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
