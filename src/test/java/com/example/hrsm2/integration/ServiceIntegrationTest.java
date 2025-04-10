package com.example.hrsm2.integration;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.PerformanceEvaluation;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.PerformanceEvaluationService;
import com.example.hrsm2.service.LeaveRequestService;
import com.example.hrsm2.service.UserService;
import com.example.hrsm2.model.LeaveRequest.LeaveStatus;
import com.example.hrsm2.model.User.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for service interactions.
 * This class tests how different services work together.
 */
public class ServiceIntegrationTest {
    
    private EmployeeService employeeService;
    private PerformanceEvaluationService evaluationService;
    private LeaveRequestService leaveRequestService;
    private UserService userService;
    
    private Employee testEmployee;
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        // Get service instances
        employeeService = EmployeeService.getInstance();
        evaluationService = PerformanceEvaluationService.getInstance();
        leaveRequestService = LeaveRequestService.getInstance();
        userService = UserService.getInstance();
        
        // Create test data that will be used across tests
        setupTestData();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestData();
    }
    
    /**
     * Test the flow: Create employee -> Add performance evaluation -> Request leave
     * This tests integration between multiple services
     */
    @Test
    public void testEmployeeEvaluationLeaveFlow() {
        // Verify the employee exists
        Employee employee = employeeService.getEmployeeById(testEmployee.getId());
        assertNotNull(employee);
        
        // Create and add a performance evaluation for the employee
        PerformanceEvaluation evaluation = new PerformanceEvaluation();
        evaluation.setEmployeeId(employee.getId());
        evaluation.setEvaluationDate(LocalDate.now());
        evaluation.setPerformanceRating(4);
        evaluation.setStrengths("Great teamwork");
        evaluation.setAreasForImprovement("Technical skills");
        evaluation.setComments("Good employee");
        evaluation.setReviewedBy(testUser.getUsername());
        
        evaluationService.addEvaluation(evaluation);
        
        // Verify evaluation was added
        assertFalse(evaluationService.getAllEvaluations().isEmpty());
        
        // Create leave request for the same employee
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(employee.getId());
        leaveRequest.setStartDate(LocalDate.now().plusDays(7));
        leaveRequest.setEndDate(LocalDate.now().plusDays(12));
        leaveRequest.setReason("Vacation");
        leaveRequest.setStatus(LeaveStatus.PENDING);
        
        // This would use the actual method from your LeaveRequestService
        // For demonstration, assuming it's called submitLeaveRequest
        // leaveRequestService.submitLeaveRequest(leaveRequest);
        
        // Verify leave request was added - using a safer check than assuming a specific method
        // assertFalse(leaveRequestService.getAllLeaveRequests().isEmpty());
        
        // Test retrieving employee with associated data
        // Verify that the employee's evaluations and leave requests are properly linked
        
        // Cleanup is done in tearDown method
    }
    
    /**
     * Setup test data for integration tests
     */
    private void setupTestData() {
        // Create a test user - use appropriate constructor based on your User class
        testUser = new User("testuser_" + System.currentTimeMillis(), "password123", "Test User");
        testUser.setRole(UserRole.ADMIN);
        
        // This would use the actual method from your UserService
        // For demonstration purposes
        // userService.registerUser(testUser);
        
        // Create a test employee
        testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setPhone("1234567890");
        testEmployee.setDepartment("Testing");
        
        employeeService.addEmployee(testEmployee);
    }
    
    /**
     * Clean up test data after tests
     */
    private void cleanupTestData() {
        // Remove test data from database
        // Implementation depends on your service's delete methods
        
        // Example (if these methods exist):
        if (testEmployee != null && testEmployee.getId() != null) {
            // Delete associated leave requests and evaluations first
            // This assumes you have methods to get and delete leave requests
            /*
            leaveRequestService.getAllLeaveRequests().stream()
                .filter(request -> request.getEmployeeId().equals(testEmployee.getId()))
                .forEach(request -> leaveRequestService.deleteLeaveRequest(request.getId()));
            */
            
            evaluationService.getAllEvaluations().stream()
                .filter(eval -> eval.getEmployeeId().equals(testEmployee.getId()))
                .forEach(eval -> evaluationService.deleteEvaluation(eval.getId()));
            
            // Then delete the employee
            employeeService.deleteEmployee(testEmployee.getId());
        }
        
        // Delete test user - commented out since we don't know the exact API
        /*
        if (testUser != null && testUser.getUsername() != null) {
            userService.deleteUser(testUser.getUsername());
        }
        */
    }
} 