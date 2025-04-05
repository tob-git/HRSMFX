package com.example.hrsm2.model;

import java.time.LocalDate;
// Removed UUID import

public class LeaveRequest {
    private Integer id; // Changed from String to Integer (nullable for new requests before DB insert)
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

    // Default constructor (optional, but useful for frameworks/libraries)
    public LeaveRequest() {
        this.status = LeaveStatus.PENDING;
        // ID is not set here, will be set by DB or after retrieval
    }

    // Constructor for creating a NEW request (before saving to DB)
    public LeaveRequest(String employeeId, LocalDate startDate, LocalDate endDate, String reason) {
        this(); // Call default constructor to set status
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        // ID remains null until saved
    }

    // Constructor for creating an object from DB data (includes ID)
    public LeaveRequest(Integer id, String employeeId, LocalDate startDate, LocalDate endDate, String reason, LeaveStatus status, String managerComments) {
        this.id = id;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.managerComments = managerComments;
    }


    // Getters and setters
    public Integer getId() { // Return type changed to Integer
        return id;
    }

    public void setId(Integer id) { // Parameter type changed to Integer
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
    public long getDurationInDays() { // Changed return type to long for ChronoUnit
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return 0;
        }
        // Use ChronoUnit for robust calculation including start/end days
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "id=" + id +
                ", employeeId='" + employeeId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}