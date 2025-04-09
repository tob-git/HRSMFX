package com.example.hrsm2.service;

import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.util.DatabaseDriver;
import java.util.List;

/**
 * Service layer for managing LeaveRequest business logic and data access.
 * Acts as an intermediary between the Controller and the DatabaseDriver.
 * Implements the Singleton pattern to ensure a single instance.
 */
public class LeaveRequestService {
    private static LeaveRequestService instance;
    private final DatabaseDriver dbDriver;

    // Default leave allowance per employee. In a real application, this might be configurable or stored per employee.
    private static final int DEFAULT_AVAILABLE_LEAVE_DAYS = 20;

    // Private constructor to enforce Singleton pattern.
    private LeaveRequestService() {
        dbDriver = DatabaseDriver.getInstance(); // Obtain the shared DatabaseDriver instance.
    }

    /**
     * Returns the singleton instance of LeaveRequestService.
     * Ensures thread-safe lazy initialization.
     *
     * @return The single instance of LeaveRequestService.
     */
    public static synchronized LeaveRequestService getInstance() {
        if (instance == null) {
            instance = new LeaveRequestService();
        }
        return instance;
    }

    // --- Database Interaction Methods ---

    /**
     * Retrieves all leave requests from the database.
     *
     * @return A list of all LeaveRequest objects.
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        return dbDriver.getAllLeaveRequests();
    }

    /**
     * Retrieves a specific leave request by its ID.
     *
     * @param id The ID of the leave request.
     * @return The LeaveRequest object if found, otherwise null.
     */
    public LeaveRequest getLeaveRequestById(int id) {
        return dbDriver.getLeaveRequestById(id);
    }

    /**
     * Retrieves all leave requests submitted by a specific employee.
     *
     * @param employeeId The ID of the employee.
     * @return A list of LeaveRequest objects for the specified employee.
     */
    public List<LeaveRequest> getLeaveRequestsForEmployee(String employeeId) {
        return dbDriver.getLeaveRequestsByEmployeeId(employeeId);
    }

    /**
     * Submits a new leave request after performing validation checks.
     * Checks for overlapping requests and sufficient available leave days.
     *
     * @param leaveRequest The LeaveRequest object to submit (ID should be null).
     * @return true if the request was successfully inserted, false otherwise.
     */
    public boolean submitLeaveRequest(LeaveRequest leaveRequest) {
        // Basic validation of essential fields.
        if (leaveRequest == null || leaveRequest.getEmployeeId() == null || leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            System.err.println("Submit failed: Invalid leave request data (null fields).");
            return false;
        }
        if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
            System.err.println("Submit failed: Start date cannot be after end date.");
            return false;
        }

        // Business logic validation: Check for overlaps and available days.
        if (hasOverlappingLeave(leaveRequest)) {
            System.err.println("Submit failed: Request overlaps with existing leave for employee " + leaveRequest.getEmployeeId());
            return false;
        }
        if (!hasEnoughAvailableDays(leaveRequest)) {
            System.err.println("Submit failed: Not enough available leave days for employee " + leaveRequest.getEmployeeId());
            return false;
        }

        // Attempt to insert into the database.
        int generatedId = dbDriver.insertLeaveRequest(leaveRequest);

        if (generatedId > 0) {
            leaveRequest.setId(generatedId); // Update the object with the database-generated ID.
            return true;
        } else {
            System.err.println("Submit failed: Database insertion error for employee " + leaveRequest.getEmployeeId());
            return false;
        }
    }

    /**
     * Approves a pending leave request.
     *
     * @param leaveRequestId  The ID of the leave request to approve.
     * @param managerComments Optional comments from the manager.
     * @return true if the request was successfully updated to APPROVED, false otherwise.
     */
    public boolean approveLeaveRequest(int leaveRequestId, String managerComments) {
        LeaveRequest request = dbDriver.getLeaveRequestById(leaveRequestId);
        // Can only approve requests that exist and are currently PENDING.
        if (request != null && request.getStatus() == LeaveRequest.LeaveStatus.PENDING) {
            request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
            request.setManagerComments(managerComments); // Store manager comments.
            return dbDriver.updateLeaveRequest(request); // Persist changes.
        }
        System.err.println("Approve failed: Request ID " + leaveRequestId + " not found or not in PENDING state.");
        return false;
    }

    /**
     * Rejects a pending leave request. Requires manager comments.
     *
     * @param leaveRequestId  The ID of the leave request to reject.
     * @param managerComments Mandatory comments explaining the rejection.
     * @return true if the request was successfully updated to REJECTED, false otherwise.
     */
    public boolean rejectLeaveRequest(int leaveRequestId, String managerComments) {
        // Manager comments are mandatory for rejection.
        if (managerComments == null || managerComments.trim().isEmpty()) {
            System.err.println("Reject failed: Manager comments are required for request ID " + leaveRequestId);
            return false;
        }
        LeaveRequest request = dbDriver.getLeaveRequestById(leaveRequestId);
        // Can only reject requests that exist and are currently PENDING.
        if (request != null && request.getStatus() == LeaveRequest.LeaveStatus.PENDING) {
            request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
            request.setManagerComments(managerComments); // Store manager comments.
            return dbDriver.updateLeaveRequest(request); // Persist changes.
        }
        System.err.println("Reject failed: Request ID " + leaveRequestId + " not found or not in PENDING state.");
        return false;
    }

    /**
     * Updates an existing leave request in the database.
     * Use with caution, consider business rules (e.g., can't update approved requests easily).
     *
     * @param leaveRequest The LeaveRequest object with updated information (must have a valid ID).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest == null || leaveRequest.getId() == null || leaveRequest.getId() <= 0) {
            System.err.println("Update failed: Invalid leave request data (null or invalid ID).");
            return false;
        }
        // Consider adding validation similar to submitLeaveRequest if updates need strict checks.
        return dbDriver.updateLeaveRequest(leaveRequest);
    }

    /**
     * Deletes a leave request from the database.
     * Consider adding business rules (e.g., only allow deleting PENDING requests).
     *
     * @param id The ID of the leave request to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteLeaveRequest(int id) {
        return dbDriver.deleteLeaveRequest(id);
    }

    // --- Calculation and Validation Logic ---

    /**
     * Calculates the total number of approved leave days used by an employee.
     * Fetches approved requests from the database for the calculation.
     *
     * @param employeeId The ID of the employee.
     * @return The total number of approved leave days.
     */
    public int getApprovedLeaveDaysForEmployee(String employeeId) {
        int totalDays = 0;
        List<LeaveRequest> approvedRequests = dbDriver.getApprovedLeaveRequestsByEmployeeId(employeeId);
        for (LeaveRequest request : approvedRequests) {
            totalDays += request.getDurationInDays(); // Sum days using the model's calculation.
        }
        return totalDays;
    }

    /**
     * Checks if a new leave request overlaps with existing, non-rejected requests for the same employee.
     *
     * @param newRequest The new leave request to check.
     * @return true if an overlap is found, false otherwise.
     */
    private boolean hasOverlappingLeave(LeaveRequest newRequest) {
        List<LeaveRequest> existingRequests = dbDriver.getLeaveRequestsByEmployeeId(newRequest.getEmployeeId());

        for (LeaveRequest existing : existingRequests) {
            // Ignore rejected requests and the request itself if it's being updated.
            if (existing.getStatus() != LeaveRequest.LeaveStatus.REJECTED &&
                    !existing.getId().equals(newRequest.getId())) { // Use .equals for Integer comparison.

                // Standard date range overlap check (inclusive): (StartA <= EndB) and (EndA >= StartB)
                if (!newRequest.getStartDate().isAfter(existing.getEndDate()) &&
                        !newRequest.getEndDate().isBefore(existing.getStartDate())) {
                    System.out.println("Overlap detected: New request [" + newRequest.getStartDate() + " - " + newRequest.getEndDate() +
                            "] overlaps with existing ID " + existing.getId() +
                            " [" + existing.getStartDate() + " - " + existing.getEndDate() + "]");
                    return true; // Overlap found.
                }
            }
        }
        return false; // No overlaps found.
    }

    /**
     * Checks if an employee has enough available leave days for a given request.
     * Compares requested days against the default allowance minus already approved days.
     *
     * @param request The leave request being submitted or updated.
     * @return true if the employee has sufficient days, false otherwise.
     */
    private boolean hasEnoughAvailableDays(LeaveRequest request) {
        long requestedDays = request.getDurationInDays();
        // If date range is invalid (0 or negative days), this check passes,
        // but it should ideally be caught by earlier validation.
        if (requestedDays <= 0) return true;

        int approvedDays = getApprovedLeaveDaysForEmployee(request.getEmployeeId());
        int availableDays = DEFAULT_AVAILABLE_LEAVE_DAYS - approvedDays;

        boolean hasEnough = requestedDays <= availableDays;
        if (!hasEnough) {
            System.out.println("Insufficient days for request: Employee " + request.getEmployeeId() +
                    ", Requested=" + requestedDays + ", Approved=" + approvedDays +
                    ", Available=" + availableDays + " (Default Allowance=" + DEFAULT_AVAILABLE_LEAVE_DAYS + ")");
        }
        return hasEnough;
    }
}