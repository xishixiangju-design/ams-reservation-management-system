package com.trae.ams.dto.dashboard;

import java.io.Serializable;

public class TechStatusDTO implements Serializable {
    private Long id;
    private String name;
    private String level;
    private String status; // IDLE, BUSY, LEAVE
    private Integer wheelSeq;
    private String currentTask; // e.g. "Serving Customer A (30min left)"

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getWheelSeq() {
        return wheelSeq;
    }

    public void setWheelSeq(Integer wheelSeq) {
        this.wheelSeq = wheelSeq;
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
    }
}
