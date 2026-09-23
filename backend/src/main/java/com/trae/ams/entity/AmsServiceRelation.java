package com.trae.ams.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 套餐与服务关联表 (用于定义套餐内包含的子服务)
 */
public class AmsServiceRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /** 父服务ID (套餐) */
    private Long parentServiceId;

    /** 子服务ID */
    private Long childServiceId;

    /** 排序 */
    private Integer sortOrder;

    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentServiceId() {
        return parentServiceId;
    }

    public void setParentServiceId(Long parentServiceId) {
        this.parentServiceId = parentServiceId;
    }

    public Long getChildServiceId() {
        return childServiceId;
    }

    public void setChildServiceId(Long childServiceId) {
        this.childServiceId = childServiceId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
