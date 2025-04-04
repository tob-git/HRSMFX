package com.example.hrsm2.model;

import java.time.LocalDate;
import java.util.UUID;

public class LeaveRequest {
    private String id;
    private String employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private String managerComments;

    public enum LeaveStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public LeaveRequest() {
        this.id = UUID.randomUUID().toString();
        this.status = LeaveStatus.PENDING;
    }

    public LeaveRequest(String employeeId, LocalDate startDate, LocalDate endDate, String reason) {
        this();
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getManagerComments() {
        return managerComments;
    }

    public void setManagerComments(String managerComments) {
        this.managerComments = managerComments;
    }

    // Utility methods
    public int getDurationInDays() {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);
    }
} 