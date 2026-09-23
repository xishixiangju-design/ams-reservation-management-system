package com.trae.ams.dto.dashboard;

import java.io.Serializable;
import java.math.BigDecimal;

public class DashboardSummaryDTO implements Serializable {
    private Integer todayAppointments;
    private BigDecimal todayRevenue;
    private Integer waitlistCount;
    private Integer violationCount;
    private Integer pendingServicesCount;

    public Integer getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(Integer todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public Integer getWaitlistCount() {
        return waitlistCount;
    }

    public void setWaitlistCount(Integer waitlistCount) {
        this.waitlistCount = waitlistCount;
    }

    public Integer getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(Integer violationCount) {
        this.violationCount = violationCount;
    }

    public Integer getPendingServicesCount() {
        return pendingServicesCount;
    }

    public void setPendingServicesCount(Integer pendingServicesCount) {
        this.pendingServicesCount = pendingServicesCount;
    }
}
