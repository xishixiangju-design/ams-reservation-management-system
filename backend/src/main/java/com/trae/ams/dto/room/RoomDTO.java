package com.trae.ams.dto.room;

import java.io.Serializable;

public class RoomDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 房间号/名 */
    private String name;

    /** SINGLE(单人), DOUBLE(双人) */
    private String type;

    /** 容纳人数 */
    private Integer capacity;

    /** 1:可用, 0:维护中 */
    private Integer status;

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
