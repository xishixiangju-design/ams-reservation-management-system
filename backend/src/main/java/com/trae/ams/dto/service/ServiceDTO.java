package com.trae.ams.dto.service;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务项目 DTO
 */
public class ServiceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 服务名称 */
    private String name;

    /** 服务名称 (中文) */
    private String nameCn;

    /** 服务名称 (日文) */
    private String nameJp;

    /** 时长 (分钟) */
    private Integer duration;

    /** 基准价格 */
    private BigDecimal price;

    /** 描述 */
    private String description;

    /** 描述 (中文) */
    private String descriptionCn;

    /** 描述 (日文) */
    private String descriptionJp;

    /** 1:上架, 0:下架 */
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public String getNameJp() {
        return nameJp;
    }

    public void setNameJp(String nameJp) {
        this.nameJp = nameJp;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionCn() {
        return descriptionCn;
    }

    public void setDescriptionCn(String descriptionCn) {
        this.descriptionCn = descriptionCn;
    }

    public String getDescriptionJp() {
        return descriptionJp;
    }

    public void setDescriptionJp(String descriptionJp) {
        this.descriptionJp = descriptionJp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
