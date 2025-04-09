package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.LeaveRequestService;


import java.time.LocalDate;
import java.util.List;

/**
 * Controller class for Leave Request operations.
 * Contains business logic for managing leave requests.
 */
public class LeaveController {
    // Services
    private final EmployeeService employeeService;
    private final LeaveRequestService leaveRequestService;
    
    // Default available leave days per employee
    private static final int DEFAULT_AVAILABLE_LEAVE_DAYS = 20;
    
    /**
     * Constructor initializes services
     */
    public LeaveController() {
        this.employeeService = EmployeeService.getInstance();
        this.leaveRequestService = LeaveRequestService.getInstance();
    }
    
    /**
     * Get all employees from the database
     * @return List of all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }
    
    /**
     * Get all leave requests from the database
     * @return List of all leave requests
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestService.getAllLeaveRequests();
    }
    
    /**
     * Get employee by ID
     * @param employeeId The employee ID
     * @return The Employee object or null if not found
     */
    public Employee getEmployeeById(String employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }
    
    /**
     * Calculate number of approved leave days for an employee
     * @param employeeId The employee ID
     * @return Number of approved leave days
     */
    public int getApprovedLeaveDaysForEmployee(String employeeId) {
        return leaveRequestService.getApprovedLeaveDaysForEmployee(employeeId);
    }
    
    /**
     * Calculate the number of days available for an employee
     * @param employeeId The employee ID
     * @return Number of available leave days
     */
    public int getAvailableLeaveDays(String employeeId) {
        int usedDays = getApprovedLeaveDaysForEmployee(employeeId);
        return Math.max(0, DEFAULT_AVAILABLE_LEAVE_DAYS - usedDays);
    }
    
    /**
     * Submit a new leave request
     * @param request The leave request to submit
     * @return true if submission was successful, false otherwise
     */
    public boolean submitLeaveRequest(LeaveRequest request) {
        return leaveRequestService.submitLeaveRequest(request);
    }
    
    /**
     * Create and submit a new leave request
     * @param employeeId The employee ID
     * @param startDate The start date of the leave
     * @param endDate The end date of the leave
     * @param reason The reason for the leave
     * @return true if submission was successful, false otherwise
     */
    public boolean submitLeaveRequest(String employeeId, LocalDate startDate, LocalDate endDate, String reason) {
        LeaveRequest request = new LeaveRequest(employeeId, startDate, endDate, reason);
        return submitLeaveRequest(request);
    }
    
    /**
     * Approve a leave request
     * @param requestId The ID of the leave request
     * @param comments Manager comments
     * @return true if approval was successful, false otherwise
     */
    public boolean approveLeaveRequest(int requestId, String comments) {
        return leaveRequestService.approveLeaveRequest(requestId, comments);
    }
    
    /**
     * Reject a leave request
     * @param requestId The ID of the leave request
     * @param comments Manager comments explaining rejection reason
     * @return true if rejection was successful, false otherwise
     */
    public boolean rejectLeaveRequest(int requestId, String comments) {
        return leaveRequestService.rejectLeaveRequest(requestId, comments);
    }
    
    /**
     * Get the default leave days allowance
     * @return The default leave days allowance per employee
     */
    public int getDefaultAvailableLeaveDays() {
        return DEFAULT_AVAILABLE_LEAVE_DAYS;
    }
}