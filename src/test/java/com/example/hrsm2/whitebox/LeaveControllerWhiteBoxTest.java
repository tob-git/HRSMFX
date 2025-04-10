package com.example.hrsm2.whitebox;

import com.example.hrsm2.controller.LeaveController;
import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.LeaveRequestService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * White box tests for LeaveController.
 * These tests focus on internal paths, branch coverage, and decision logic.
 */
public class LeaveControllerWhiteBoxTest {
    
    @Mock
    private EmployeeService employeeService;
    
    @Mock
    private LeaveRequestService leaveRequestService;
    
    @InjectMocks
    private LeaveController controller;
    
    private MockedStatic<EmployeeService> mockedEmployeeService;
    private MockedStatic<LeaveRequestService> mockedLeaveRequestService;
    
    // Keep track of mocked employees and leave requests
    private final List<Employee> testEmployees = new ArrayList<>();
    private final List<LeaveRequest> testLeaveRequests = new ArrayList<>();
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Mock the static getInstance methods
        mockedEmployeeService = mockStatic(EmployeeService.class);
        mockedLeaveRequestService = mockStatic(LeaveRequestService.class);
        
        mockedEmployeeService.when(EmployeeService::getInstance).thenReturn(employeeService);
        mockedLeaveRequestService.when(LeaveRequestService::getInstance).thenReturn(leaveRequestService);
        
        // Setup test data
        setupTestData();
        
        // Create a fresh controller for each test
        controller = new LeaveController();
        
        // Use reflection to inject mocks into the controller
        injectMocks();
    }
    
    @AfterEach
    public void tearDown() {
        mockedEmployeeService.close();
        mockedLeaveRequestService.close();
        testEmployees.clear();
        testLeaveRequests.clear();
    }
    
    /**
     * Setup test data for employees and leave requests
     */
    private void setupTestData() {
        // Create test employees
        Employee emp1 = new Employee();
        emp1.setId("emp1");
        emp1.setFirstName("John");
        emp1.setLastName("Doe");
        
        Employee emp2 = new Employee();
        emp2.setId("emp2");
        emp2.setFirstName("Jane");
        emp2.setLastName("Smith");
        
        testEmployees.add(emp1);
        testEmployees.add(emp2);
        
        // Create test leave requests
        LeaveRequest req1 = new LeaveRequest();
        req1.setId(1);
        req1.setEmployeeId("emp1");
        req1.setStartDate(LocalDate.now().minusDays(10));
        req1.setEndDate(LocalDate.now().minusDays(5));
        req1.setReason("Vacation");
        req1.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        
        LeaveRequest req2 = new LeaveRequest();
        req2.setId(2);
        req2.setEmployeeId("emp1");
        req2.setStartDate(LocalDate.now().plusDays(5));
        req2.setEndDate(LocalDate.now().plusDays(10));
        req2.setReason("Family event");
        req2.setStatus(LeaveRequest.LeaveStatus.PENDING);
        
        LeaveRequest req3 = new LeaveRequest();
        req3.setId(3);
        req3.setEmployeeId("emp2");
        req3.setStartDate(LocalDate.now().plusDays(15));
        req3.setEndDate(LocalDate.now().plusDays(25));
        req3.setReason("Medical");
        req3.setStatus(LeaveRequest.LeaveStatus.PENDING);
        
        testLeaveRequests.add(req1);
        testLeaveRequests.add(req2);
        testLeaveRequests.add(req3);
        
        // Setup mock behavior
        when(employeeService.getAllEmployees()).thenReturn(testEmployees);
        when(leaveRequestService.getAllLeaveRequests()).thenReturn(testLeaveRequests);
        
        for (Employee emp : testEmployees) {
            when(employeeService.getEmployeeById(emp.getId())).thenReturn(emp);
        }
    }
    
    /**
     * Helper method to inject mocks using reflection
     */
    private void injectMocks() throws Exception {
        // Inject mocked services into the controller
        setPrivateField(controller, "employeeService", employeeService);
        setPrivateField(controller, "leaveRequestService", leaveRequestService);
    }
    
    /**
     * Helper method to set private fields via reflection
     */
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = LeaveController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    // =================== Test Cases for Public Methods ===================
    
    @Test
    @DisplayName("Test getAllEmployees returns list from service")
    public void testGetAllEmployees() {
        // Act
        List<Employee> result = controller.getAllEmployees();
        
        // Assert
        assertEquals(testEmployees, result);
        verify(employeeService).getAllEmployees();
    }
    
    @Test
    @DisplayName("Test getAllLeaveRequests returns list from service")
    public void testGetAllLeaveRequests() {
        // Act
        List<LeaveRequest> result = controller.getAllLeaveRequests();
        
        // Assert
        assertEquals(testLeaveRequests, result);
        verify(leaveRequestService).getAllLeaveRequests();
    }
    
    @Test
    @DisplayName("Test getEmployeeById delegates to service")
    public void testGetEmployeeById() {
        // Arrange
        String employeeId = "emp1";
        
        // Act
        Employee result = controller.getEmployeeById(employeeId);
        
        // Assert
        assertEquals(testEmployees.get(0), result);
        verify(employeeService).getEmployeeById(employeeId);
    }
    
    @Test
    @DisplayName("Test getApprovedLeaveDaysForEmployee delegates to service")
    public void testGetApprovedLeaveDaysForEmployee() {
        // Arrange
        String employeeId = "emp1";
        int expectedDays = 10;
        when(leaveRequestService.getApprovedLeaveDaysForEmployee(employeeId)).thenReturn(expectedDays);
        
        // Act
        int result = controller.getApprovedLeaveDaysForEmployee(employeeId);
        
        // Assert
        assertEquals(expectedDays, result);
        verify(leaveRequestService).getApprovedLeaveDaysForEmployee(employeeId);
    }
    
    @Test
    @DisplayName("Test getAvailableLeaveDays when used days less than default")
    public void testGetAvailableLeaveDays_NormalCase() {
        // Arrange
        String employeeId = "emp1";
        int usedDays = 10;
        when(leaveRequestService.getApprovedLeaveDaysForEmployee(employeeId)).thenReturn(usedDays);
        
        // Act
        int result = controller.getAvailableLeaveDays(employeeId);
        
        // Assert
        assertEquals(10, result); // 20 default - 10 used = 10 available
        verify(leaveRequestService).getApprovedLeaveDaysForEmployee(employeeId);
    }
    
    @Test
    @DisplayName("Test getAvailableLeaveDays when used days equal to default")
    public void testGetAvailableLeaveDays_ZeroAvailable() {
        // Arrange
        String employeeId = "emp1";
        int usedDays = 20; // Using all available days
        when(leaveRequestService.getApprovedLeaveDaysForEmployee(employeeId)).thenReturn(usedDays);
        
        // Act
        int result = controller.getAvailableLeaveDays(employeeId);
        
        // Assert
        assertEquals(0, result); // 20 default - 20 used = 0 available
        verify(leaveRequestService).getApprovedLeaveDaysForEmployee(employeeId);
    }
    
    @Test
    @DisplayName("Test getAvailableLeaveDays when used days more than default")
    public void testGetAvailableLeaveDays_NegativeCase() {
        // Arrange
        String employeeId = "emp1";
        int usedDays = 25; // More than available days
        when(leaveRequestService.getApprovedLeaveDaysForEmployee(employeeId)).thenReturn(usedDays);
        
        // Act
        int result = controller.getAvailableLeaveDays(employeeId);
        
        // Assert
        assertEquals(0, result); // Should return 0, not negative
        verify(leaveRequestService).getApprovedLeaveDaysForEmployee(employeeId);
    }
    
    @Test
    @DisplayName("Test submitLeaveRequest with LeaveRequest object")
    public void testSubmitLeaveRequest_WithRequestObject() {
        // Arrange
        LeaveRequest request = new LeaveRequest();
        when(leaveRequestService.submitLeaveRequest(request)).thenReturn(true);
        
        // Act
        boolean result = controller.submitLeaveRequest(request);
        
        // Assert
        assertTrue(result);
        verify(leaveRequestService).submitLeaveRequest(request);
    }
    
    @Test
    @DisplayName("Test submitLeaveRequest with individual parameters")
    public void testSubmitLeaveRequest_WithParameters() {
        // Arrange
        String employeeId = "emp1";
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);
        String reason = "Testing";
        
        when(leaveRequestService.submitLeaveRequest(any(LeaveRequest.class))).thenReturn(true);
        
        // Act
        boolean result = controller.submitLeaveRequest(employeeId, startDate, endDate, reason);
        
        // Assert
        assertTrue(result);
        verify(leaveRequestService).submitLeaveRequest(any(LeaveRequest.class));
    }
    
    @Test
    @DisplayName("Test submitLeaveRequest when service returns false")
    public void testSubmitLeaveRequest_Failure() {
        // Arrange
        LeaveRequest request = new LeaveRequest();
        when(leaveRequestService.submitLeaveRequest(request)).thenReturn(false);
        
        // Act
        boolean result = controller.submitLeaveRequest(request);
        
        // Assert
        assertFalse(result);
        verify(leaveRequestService).submitLeaveRequest(request);
    }
    
    @Test
    @DisplayName("Test approveLeaveRequest delegates to service")
    public void testApproveLeaveRequest() {
        // Arrange
        int requestId = 1;
        String comments = "Approved";
        when(leaveRequestService.approveLeaveRequest(requestId, comments)).thenReturn(true);
        
        // Act
        boolean result = controller.approveLeaveRequest(requestId, comments);
        
        // Assert
        assertTrue(result);
        verify(leaveRequestService).approveLeaveRequest(requestId, comments);
    }
    
    @Test
    @DisplayName("Test approveLeaveRequest when service returns false")
    public void testApproveLeaveRequest_Failure() {
        // Arrange
        int requestId = 1;
        String comments = "Approved";
        when(leaveRequestService.approveLeaveRequest(requestId, comments)).thenReturn(false);
        
        // Act
        boolean result = controller.approveLeaveRequest(requestId, comments);
        
        // Assert
        assertFalse(result);
        verify(leaveRequestService).approveLeaveRequest(requestId, comments);
    }
    
    @Test
    @DisplayName("Test rejectLeaveRequest delegates to service")
    public void testRejectLeaveRequest() {
        // Arrange
        int requestId = 1;
        String comments = "Rejected";
        when(leaveRequestService.rejectLeaveRequest(requestId, comments)).thenReturn(true);
        
        // Act
        boolean result = controller.rejectLeaveRequest(requestId, comments);
        
        // Assert
        assertTrue(result);
        verify(leaveRequestService).rejectLeaveRequest(requestId, comments);
    }
    
    @Test
    @DisplayName("Test rejectLeaveRequest when service returns false")
    public void testRejectLeaveRequest_Failure() {
        // Arrange
        int requestId = 1;
        String comments = "Rejected";
        when(leaveRequestService.rejectLeaveRequest(requestId, comments)).thenReturn(false);
        
        // Act
        boolean result = controller.rejectLeaveRequest(requestId, comments);
        
        // Assert
        assertFalse(result);
        verify(leaveRequestService).rejectLeaveRequest(requestId, comments);
    }
    
    @Test
    @DisplayName("Test getDefaultAvailableLeaveDays returns correct constant")
    public void testGetDefaultAvailableLeaveDays() {
        // Act
        int result = controller.getDefaultAvailableLeaveDays();
        
        // Assert
        assertEquals(20, result); // Should match the constant in the controller
    }
} 