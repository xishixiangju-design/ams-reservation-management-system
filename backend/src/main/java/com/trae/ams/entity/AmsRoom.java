package com.trae.ams.entity;

import java.io.Serializable;

/**
 * 房间表
 */
public class AmsRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属门店ID */
    private Long storeId;

    /** 房间号/名 */
    private String name;

    /** SINGLE(单人), DOUBLE(双人) */
    private String type;

    /** 容纳人数 */
    private Integer capacity;

    /** 1:可用, 0:维护中 */
    private Integer status;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
