package com.example.hrsm2.service;

import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.util.DatabaseDriver;
import java.util.List;

public class LeaveRequestService {
    private static LeaveRequestService instance;
    private final DatabaseDriver dbDriver; // Use DatabaseDriver instance

    // EmployeeService might still be needed for some logic, ensure it uses DB too
    // private final EmployeeService employeeService;

    // Default available days - This might need a more sophisticated source (e.g., Employee record or config)
    private static final int DEFAULT_AVAILABLE_LEAVE_DAYS = 20;

    private LeaveRequestService() {
        dbDriver = DatabaseDriver.getInstance(); // Get singleton instance
        // employeeService = EmployeeService.getInstance(); // If needed
    }

    public static synchronized LeaveRequestService getInstance() {
        if (instance == null) {
            instance = new LeaveRequestService();
        }
        return instance;
    }

    // --- CRUD Operations using DatabaseDriver ---

    public List<LeaveRequest> getAllLeaveRequests() {
        return dbDriver.getAllLeaveRequests();
    }

    public LeaveRequest getLeaveRequestById(int id) { // Parameter changed to int
        return dbDriver.getLeaveRequestById(id);
    }

    public List<LeaveRequest> getLeaveRequestsForEmployee(String employeeId) {
        return dbDriver.getLeaveRequestsByEmployeeId(employeeId);
    }

    public boolean submitLeaveRequest(LeaveRequest leaveRequest) {
        // Validate inputs (dates, reason etc.) - Controller handles UI validation
        if (leaveRequest == null || leaveRequest.getEmployeeId() == null || leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            System.err.println("Submit failed: Invalid leave request data.");
            return false;
        }

        // Check for overlapping leave requests from DB
        if (hasOverlappingLeave(leaveRequest)) {
            System.err.println("Submit failed: Request overlaps with existing leave.");
            return false;
        }

        // Check if employee has enough available leave days (based on DB data)
        if (!hasEnoughAvailableDays(leaveRequest)) {
            System.err.println("Submit failed: Not enough available leave days.");
            return false;
        }

        // Insert into database
        int generatedId = dbDriver.insertLeaveRequest(leaveRequest);

        if (generatedId > 0) {
            leaveRequest.setId(generatedId); // Set the generated ID back to the object
            return true;
        } else {
            return false;
        }
    }

    public boolean approveLeaveRequest(int leaveRequestId, String managerComments) { // Parameter changed to int, added comments
        LeaveRequest request = dbDriver.getLeaveRequestById(leaveRequestId);
        if (request != null && request.getStatus() == LeaveRequest.LeaveStatus.PENDING) {
            request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            request.setManagerComments(managerComments); // Set comments on approval too (optional)
            return dbDriver.updateLeaveRequest(request); // Update in DB
        }
        return false;
    }

    public boolean rejectLeaveRequest(int leaveRequestId, String managerComments) { // Parameter changed to int, added comments
        if (managerComments == null || managerComments.trim().isEmpty()) {
            System.err.println("Reject failed: Manager comments are required.");
            return false; // Enforce comments on rejection
        }
        LeaveRequest request = dbDriver.getLeaveRequestById(leaveRequestId);
        if (request != null && request.getStatus() == LeaveRequest.LeaveStatus.PENDING) {
            request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
            request.setManagerComments(managerComments); // Set comments
            return dbDriver.updateLeaveRequest(request); // Update in DB
        }
        return false;
    }

    // Update might be less common from UI, but useful internally
    public boolean updateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest == null || leaveRequest.getId() == null || leaveRequest.getId() <= 0) {
            return false;
        }
        // Add validation/checks if needed before updating (e.g., overlap, available days)
        return dbDriver.updateLeaveRequest(leaveRequest);
    }

    public boolean deleteLeaveRequest(int id) { // Parameter changed to int
        // Maybe add checks: only delete PENDING requests?
        return dbDriver.deleteLeaveRequest(id);
    }

    // --- Calculation Logic using DatabaseDriver ---

    public int getApprovedLeaveDaysForEmployee(String employeeId) {
        int totalDays = 0;
        // Fetch only approved requests from DB
        List<LeaveRequest> approvedRequests = dbDriver.getApprovedLeaveRequestsByEmployeeId(employeeId);
        for (LeaveRequest request : approvedRequests) {
            totalDays += request.getDurationInDays(); // Use model's calculation method
        }
        return totalDays;
    }

    // Check for overlaps against DB data
    private boolean hasOverlappingLeave(LeaveRequest newRequest) {
        // Fetch potentially conflicting requests (same employee, not rejected)
        List<LeaveRequest> existingRequests = dbDriver.getLeaveRequestsByEmployeeId(newRequest.getEmployeeId());

        for (LeaveRequest existing : existingRequests) {
            // Skip rejected requests and the request itself if it has an ID (during update)
            if (existing.getStatus() != LeaveRequest.LeaveStatus.REJECTED &&
                    !existing.getId().equals(newRequest.getId())) { // Compare Integer IDs

                // Check for date overlap (inclusive)
                // Overlap exists if:
                // (newStart <= existingEnd) and (newEnd >= existingStart)
                if (!newRequest.getStartDate().isAfter(existing.getEndDate()) &&
                        !newRequest.getEndDate().isBefore(existing.getStartDate())) {
                    System.out.println("Overlap detected with request ID: " + existing.getId());
                    return true; // Overlap found
                }
            }
        }
        return false; // No overlaps found
    }

    // Check available days against DB data
    private boolean hasEnoughAvailableDays(LeaveRequest request) {
        // Calculate requested days
        long requestedDays = request.getDurationInDays();
        if (requestedDays <= 0) return true; // Or handle invalid date range earlier

        // Calculate already approved days from DB
        int approvedDays = getApprovedLeaveDaysForEmployee(request.getEmployeeId());

        // Calculate available days
        int availableDays = DEFAULT_AVAILABLE_LEAVE_DAYS - approvedDays;

        // // If updating an already approved request, add its days back temporarily for the check
        // // This logic is complex and potentially racy. Simpler to just check against current state.
        // if (request.getId() != null && request.getId() > 0) {
        //     LeaveRequest existing = dbDriver.getLeaveRequestById(request.getId());
        //     if (existing != null && existing.getStatus() == LeaveRequest.LeaveStatus.APPROVED) {
        //          availableDays += existing.getDurationInDays();
        //     }
        // }

        return requestedDays <= availableDays;
    }
}