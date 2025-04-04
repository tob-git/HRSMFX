package com.example.hrsm2.service;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeaveRequestService {
    private static LeaveRequestService instance;
    private final Map<String, LeaveRequest> leaveRequests = new HashMap<>();
    private final EmployeeService employeeService;

    private LeaveRequestService() {
        employeeService = EmployeeService.getInstance();
    }

    public static synchronized LeaveRequestService getInstance() {
        if (instance == null) {
            instance = new LeaveRequestService();
        }
        return instance;
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return new ArrayList<>(leaveRequests.values());
    }

    public LeaveRequest getLeaveRequestById(String id) {
        return leaveRequests.get(id);
    }

    public List<LeaveRequest> getLeaveRequestsForEmployee(String employeeId) {
        List<LeaveRequest> result = new ArrayList<>();
        for (LeaveRequest request : leaveRequests.values()) {
            if (request.getEmployeeId().equals(employeeId)) {
                result.add(request);
            }
        }
        return result;
    }

    public boolean submitLeaveRequest(LeaveRequest leaveRequest) {
        // Check for overlapping leave requests
        if (hasOverlappingLeave(leaveRequest)) {
            return false;
        }
        
        // Check if employee has enough available leave days
        if (!hasEnoughAvailableDays(leaveRequest)) {
            return false;
        }
        
        // Set ID if not already set
        if (leaveRequest.getId() == null || leaveRequest.getId().isEmpty()) {
            leaveRequest.setId(UUID.randomUUID().toString());
        }
        
        // Add to map
        leaveRequests.put(leaveRequest.getId(), leaveRequest);
        return true;
    }

    public boolean approveLeaveRequest(String leaveRequestId) {
        LeaveRequest request = leaveRequests.get(leaveRequestId);
        if (request != null && request.getStatus() == LeaveRequest.LeaveStatus.PENDING) {
            request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            return true;
        }
        return false;
    }

    public boolean rejectLeaveRequest(String leaveRequestId) {
        LeaveRequest request = leaveRequests.get(leaveRequestId);
        if (request != null && request.getStatus() == LeaveRequest.LeaveStatus.PENDING) {
            request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
            return true;
        }
        return false;
    }

    public void updateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getId() != null && !leaveRequest.getId().isEmpty()) {
            leaveRequests.put(leaveRequest.getId(), leaveRequest);
        }
    }

    public boolean deleteLeaveRequest(String id) {
        return leaveRequests.remove(id) != null;
    }

    public int getApprovedLeaveDaysForEmployee(String employeeId) {
        int totalDays = 0;
        for (LeaveRequest request : leaveRequests.values()) {
            if (request.getEmployeeId().equals(employeeId) && 
                request.getStatus() == LeaveRequest.LeaveStatus.APPROVED) {
                long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
                totalDays += days;
            }
        }
        return totalDays;
    }

    private boolean hasOverlappingLeave(LeaveRequest newRequest) {
        for (LeaveRequest existing : leaveRequests.values()) {
            if (existing.getEmployeeId().equals(newRequest.getEmployeeId()) &&
                existing.getStatus() != LeaveRequest.LeaveStatus.REJECTED &&
                !existing.getId().equals(newRequest.getId())) {
                
                // Check for date overlap
                if (!(newRequest.getEndDate().isBefore(existing.getStartDate()) ||
                      newRequest.getStartDate().isAfter(existing.getEndDate()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasEnoughAvailableDays(LeaveRequest request) {
        // Default available days is 20 days per year
        final int DEFAULT_AVAILABLE_DAYS = 20;
        
        // Calculate requested days
        long requestedDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        
        // Calculate already approved days
        int approvedDays = getApprovedLeaveDaysForEmployee(request.getEmployeeId());
        
        // Calculate available days
        int availableDays = DEFAULT_AVAILABLE_DAYS - approvedDays;
        
        // If updating an already approved request, add its days back to available
        if (request.getId() != null && !request.getId().isEmpty()) {
            LeaveRequest existing = leaveRequests.get(request.getId());
            if (existing != null && existing.getStatus() == LeaveRequest.LeaveStatus.APPROVED) {
                long existingDays = ChronoUnit.DAYS.between(existing.getStartDate(), existing.getEndDate()) + 1;
                availableDays += existingDays;
            }
        }
        
        return requestedDays <= availableDays;
    }
} 