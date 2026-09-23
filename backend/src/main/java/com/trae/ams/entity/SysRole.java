package com.trae.ams.entity;

import java.io.Serializable;

/**
 * 角色表
 */
public class SysRole implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 角色编码 (ROLE_ADMIN) */
    private String code;

    /** 角色名称 (管理员) */
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
