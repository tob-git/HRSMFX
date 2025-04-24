package com.example.hrsm2.whitebox;

import com.example.hrsm2.controller.PerformanceController;
import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.PerformanceEvaluation;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.PerformanceEvaluationService;
import com.example.hrsm2.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * White box tests for the PerformanceController.
 */
public class PerformanceControllerWhiteBoxTest {

    // Use lenient mocks to avoid unnecessary stubbing errors
    @Mock
    private EmployeeService mockEmployeeService;

    @Mock
    private PerformanceEvaluationService mockEvaluationService;

    @Mock
    private UserService mockUserService;

    // Do not use @InjectMocks - we'll set up the controller manually
    private PerformanceController controller;

    private User mockUser;
    private Employee existingEmployee;
    private PerformanceEvaluation existingEvaluation;
    private String employeeId;
    private String evaluationId;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize mocks manually
        MockitoAnnotations.openMocks(this);
        
        // Create test data
        employeeId = "emp123";
        existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setFirstName("John");
        existingEmployee.setLastName("Doe");
        existingEmployee.setEmail("john.doe@example.com");
        existingEmployee.setPhone("1234567890");
        existingEmployee.setHireDate(LocalDate.now());
        existingEmployee.setDepartment("IT");
        existingEmployee.setJobTitle("Developer");
        existingEmployee.setSalary(50000.0);
        
        evaluationId = "eval123";
        existingEvaluation = new PerformanceEvaluation();
        existingEvaluation.setId(evaluationId);
        existingEvaluation.setEmployeeId(employeeId);
        existingEvaluation.setPerformanceRating(4);
        existingEvaluation.setStrengths("Strengths");
        existingEvaluation.setAreasForImprovement("Improvements");
        existingEvaluation.setComments("Some comments");
        existingEvaluation.setReviewedBy("Manager");
        
        // Set up a mock user
        mockUser = new User("user1", "password123", "Mock User", User.UserRole.HR_ADMIN);
        
        // Create controller with mock services
        controller = new PerformanceController();

        // Reflectively set the mocked services
        setPrivateField(controller, "employeeService", mockEmployeeService);
        setPrivateField(controller, "evaluationService", mockEvaluationService);
        setPrivateField(controller, "userService", mockUserService);
        setPrivateField(controller, "currentUser", mockUser);
        
        // General stubbing that applies to most tests
        when(mockUserService.getCurrentUser()).thenReturn(mockUser);
    }

    /**
     * Helper method to set private fields via reflection
     */
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = PerformanceController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Helper method to access private methods via reflection
     */
    private Method getPrivateMethod(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = PerformanceController.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    @Test
    @DisplayName("Test getAllEmployees")
    void testGetAllEmployees() {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        employees.add(existingEmployee);
        when(mockEmployeeService.getAllEmployees()).thenReturn(employees);

        // Act
        List<Employee> result = controller.getAllEmployees();

        // Assert
        assertEquals(1, result.size());
        assertEquals(employeeId, result.get(0).getId());
        verify(mockEmployeeService).getAllEmployees();
    }

    @Test
    @DisplayName("Test getAllEvaluations")
    void testGetAllEvaluations() {
        // Arrange
        List<PerformanceEvaluation> evaluations = new ArrayList<>();
        evaluations.add(existingEvaluation);
        when(mockEvaluationService.getAllEvaluations()).thenReturn(evaluations);

        // Act
        List<PerformanceEvaluation> result = controller.getAllEvaluations();

        // Assert
        assertEquals(1, result.size());
        assertEquals(evaluationId, result.get(0).getId());
        verify(mockEvaluationService).getAllEvaluations();
    }

    @Test
    @DisplayName("Test getEmployeeById")
    void testGetEmployeeById() {
        // Arrange
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);
        when(mockEmployeeService.getEmployeeById("unknown")).thenReturn(null);

        // Act
        Employee e = controller.getEmployeeById(employeeId);

        // Assert
        assertNotNull(e);
        assertEquals(employeeId, e.getId());
        verify(mockEmployeeService).getEmployeeById(employeeId);

        // Test with unknown ID
        Employee notFound = controller.getEmployeeById("unknown");
        assertNull(notFound);
        verify(mockEmployeeService).getEmployeeById("unknown");
    }

    @Test
    @DisplayName("Test addEvaluation success")
    void testAddEvaluationSuccess() {
        // Arrange
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);
        doNothing().when(mockEvaluationService).addEvaluation(any(PerformanceEvaluation.class));

        // Act
        boolean result = controller.addEvaluation(
                employeeId,
                LocalDate.now(),
                5,
                "Strong Skills",
                "Needs improvement on X",
                "No additional comments"
        );

        // Assert
        assertTrue(result);
        verify(mockEvaluationService).addEvaluation(any(PerformanceEvaluation.class));
    }

    @Test
    @DisplayName("Test addEvaluation failure when employee doesn't exist")
    void testAddEvaluationFailureNoEmployee() throws Exception {
        // Arrange - No need to mock getEmployeeById for nonexistent ID - default is null
        
        // Act & Assert
        boolean result = controller.addEvaluation(
                "nonexistent",
                LocalDate.now(),
                3,
                "Strengths",
                "Improvements",
                "Comments"
        );
        
        assertFalse(result);
        verify(mockEvaluationService, never()).addEvaluation(any());
    }

    @Test
    @DisplayName("Test updateEvaluation success")
    void testUpdateEvaluationSuccess() {
        // Arrange
        when(mockEvaluationService.getEvaluationById(evaluationId)).thenReturn(existingEvaluation);
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);
        doNothing().when(mockEvaluationService).updateEvaluation(any(PerformanceEvaluation.class));

        // Act
        boolean result = controller.updateEvaluation(
                evaluationId,
                employeeId,
                LocalDate.now(),
                3,
                "New Strengths",
                "New Improvements",
                "New Comments"
        );

        // Assert
        assertTrue(result);
        verify(mockEvaluationService).updateEvaluation(any(PerformanceEvaluation.class));
    }

    @Test
    @DisplayName("Test updateEvaluation fails when evaluation not found")
    void testUpdateEvaluationNotFound() {
        // Arrange
        when(mockEvaluationService.getEvaluationById("unknownEval")).thenReturn(null);
        // No need to mock the employee since we should return early if evaluation is null

        // Act
        boolean result = controller.updateEvaluation(
                "unknownEval",
                employeeId,
                LocalDate.now(),
                4,
                "Strengths",
                "Improvements",
                "Comments"
        );

        // Assert
        assertFalse(result);
        verify(mockEvaluationService, never()).updateEvaluation(any());
    }

    @Test
    @DisplayName("Test updateEvaluation fails on validation error")
    void testUpdateEvaluationValidationError() {
        // Arrange
        when(mockEvaluationService.getEvaluationById(evaluationId)).thenReturn(existingEvaluation);
        // No need to mock nonexistent employee - default behavior is to return null

        // Act
        boolean result = controller.updateEvaluation(
                evaluationId,
                "nonexistent",
                LocalDate.now(),
                4,
                "Strengths",
                "Improvements",
                "Comments"
        );

        // Assert
        assertFalse(result);
        verify(mockEvaluationService, never()).updateEvaluation(any());
    }

    @Test
    @DisplayName("Test deleteEvaluation success")
    void testDeleteEvaluationSuccess() {
        // Arrange
        doNothing().when(mockEvaluationService).deleteEvaluation(evaluationId);

        // Act
        boolean result = controller.deleteEvaluation(evaluationId);

        // Assert
        assertTrue(result);
        verify(mockEvaluationService).deleteEvaluation(evaluationId);
    }

    @Test
    @DisplayName("Test deleteEvaluation failure")
    void testDeleteEvaluationFailure() {
        // Arrange
        doThrow(new RuntimeException("Test exception")).when(mockEvaluationService).deleteEvaluation("invalid");

        // Act
        boolean result = controller.deleteEvaluation("invalid");

        // Assert
        assertFalse(result);
        verify(mockEvaluationService).deleteEvaluation("invalid");
    }

    @Test
    @DisplayName("Test getEvaluationById")
    void testGetEvaluationById() {
        // Arrange
        when(mockEvaluationService.getEvaluationById(evaluationId)).thenReturn(existingEvaluation);
        when(mockEvaluationService.getEvaluationById("unknownEval")).thenReturn(null);

        // Act
        PerformanceEvaluation eval = controller.getEvaluationById(evaluationId);

        // Assert
        assertNotNull(eval);
        assertEquals(employeeId, eval.getEmployeeId());
        verify(mockEvaluationService).getEvaluationById(evaluationId);

        // Test with unknown ID
        PerformanceEvaluation evalUnknown = controller.getEvaluationById("unknownEval");
        assertNull(evalUnknown);
        verify(mockEvaluationService).getEvaluationById("unknownEval");
    }

    @Test
    @DisplayName("Test getRatingDescription covers switch cases and default")
    void testGetRatingDescription() {
        assertEquals("Poor", controller.getRatingDescription(1));
        assertEquals("Below Average", controller.getRatingDescription(2));
        assertEquals("Average", controller.getRatingDescription(3));
        assertEquals("Good", controller.getRatingDescription(4));
        assertEquals("Excellent", controller.getRatingDescription(5));
        // Test default (outside 1-5)
        assertEquals("Not Rated", controller.getRatingDescription(6));
        assertEquals("Not Rated", controller.getRatingDescription(0));
    }

    @Test
    @DisplayName("Test getCurrentUser and setCurrentUser")
    void testCurrentUserMethods() {
        // Create a new user to test setter/getter
        User testUser = new User("test", "pass", "Test User", User.UserRole.HR_ADMIN);
        
        // Test the setter
        controller.setCurrentUser(testUser);
        
        // Test the getter
        assertEquals(testUser, controller.getCurrentUser());
    }
    
    @Test
    @DisplayName("Test private validateEvaluationData method with null employee ID")
    void testValidateEvaluationDataNullEmployeeId() throws Exception {
        // Get access to private method
        Method validateMethod = getPrivateMethod("validateEvaluationData", 
                String.class, LocalDate.class, String.class, String.class);
        
        // Test with null employee ID
        Exception exception = assertThrows(Exception.class, () -> {
            try {
                validateMethod.invoke(controller, null, LocalDate.now(), "Strengths", "Improvements");
            } catch (Exception e) {
                throw e.getCause(); // Unwrap the InvocationTargetException
            }
        });

        assertInstanceOf(IllegalArgumentException.class, exception);
        assertTrue(exception.getMessage().contains("Employee is required"));
    }
    
    @Test
    @DisplayName("Test private validateEvaluationData method with empty strengths")
    void testValidateEvaluationDataEmptyStrengths() throws Exception {
        // Arrange
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);
        
        // Get access to private method
        Method validateMethod = getPrivateMethod("validateEvaluationData", 
                String.class, LocalDate.class, String.class, String.class);
        
        // Test with empty strengths
        Exception exception = assertThrows(Exception.class, () -> {
            try {
                validateMethod.invoke(controller, employeeId, LocalDate.now(), "", "Improvements");
            } catch (Exception e) {
                throw e.getCause(); // Unwrap the InvocationTargetException
            }
        });

        assertInstanceOf(IllegalArgumentException.class, exception);
        assertTrue(exception.getMessage().contains("Strengths is required"));
    }
    @Test
    @DisplayName("Test updateEvaluation fails when improvement is null")
    void testUpdateEvaluationFailsWhenImprovementIsNull() {
        // Arrange
        when(mockEvaluationService.getEvaluationById(evaluationId)).thenReturn(existingEvaluation);
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);

        // Act
        boolean result = controller.updateEvaluation(
                evaluationId,
                employeeId,
                LocalDate.now(),
                4,
                "Strengths",
                null, // Null improvement
                "Comments"
        );

        // Assert
        assertFalse(result);
        verify(mockEvaluationService, never()).updateEvaluation(any());
    }

    @Test
    @DisplayName("Test updateEvaluation fails when improvement is empty")
    void testUpdateEvaluationFailsWhenImprovementIsEmpty() {
        // Arrange
        when(mockEvaluationService.getEvaluationById(evaluationId)).thenReturn(existingEvaluation);
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);

        // Act
        boolean result = controller.updateEvaluation(
                evaluationId,
                employeeId,
                LocalDate.now(),
                4,
                "Strengths",
                "   ", // Empty improvement (whitespace)
                "Comments"
        );

        // Assert
        assertFalse(result);
        verify(mockEvaluationService, never()).updateEvaluation(any());
    }

    @Test
    @DisplayName("Test updateEvaluation fails when evaluation date is null")
    void testUpdateEvaluationFailsWhenEvaluationDateIsNull() {
        // Arrange
        when(mockEvaluationService.getEvaluationById(evaluationId)).thenReturn(existingEvaluation);
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);

        // Act
        boolean result = controller.updateEvaluation(
                evaluationId,
                employeeId,
                null, // Null evaluation date
                4,
                "Strengths",
                "Improvements",
                "Comments"
        );

        // Assert
        assertFalse(result);
        verify(mockEvaluationService, never()).updateEvaluation(any());
    }
    @Test
    @DisplayName("Test updateEvaluation returns false when evaluation is null")
    void testUpdateEvaluationReturnsFalseWhenEvaluationIsNull() {
        // Arrange
        when(mockEvaluationService.getEvaluationById(evaluationId)).thenReturn(null);
        when(mockEmployeeService.getEmployeeById(employeeId)).thenReturn(existingEmployee);
        // Act
        boolean result = controller.updateEvaluation(
                evaluationId,
                employeeId,
                LocalDate.now(),
                4,
                "Strengths",
                "Improvements",
                "Comments"
        );

        // Assert
        assertFalse(result);
        verify(mockEvaluationService, never()).updateEvaluation(any());
    }
    @Test
    @DisplayName("Test validateEvaluationData with blank employeeId (only spaces)")
    void testValidateEvaluationDataWithBlankEmployeeId() throws Exception {
        // Access the private method using reflection
        Method validateMethod = getPrivateMethod("validateEvaluationData",
                String.class, LocalDate.class, String.class, String.class);

        // Pass employeeId as spaces to trigger employeeId.trim().isEmpty() == true
        String blankEmployeeId = "   ";  // Spaces only

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            try {
                validateMethod.invoke(controller, blankEmployeeId, LocalDate.now(), "Strengths", "Improvements");
            } catch (Exception e) {
                throw e.getCause();  // Unwrap InvocationTargetException
            }
        });

        // Verify
        assertInstanceOf(IllegalArgumentException.class, exception);
        assertTrue(exception.getMessage().contains("Employee is required"),
                "Error message should mention that Employee is required");
    }

    @Test
    @DisplayName("Test validateEvaluationData with null strengths ")
    void testValidateEvaluationDataWithNullStrengths() throws Exception {
        // Access the private method via reflection
        Method validateMethod = getPrivateMethod("validateEvaluationData",
                String.class, LocalDate.class, String.class, String.class);



        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            try {
                validateMethod.invoke(controller, "emp123", LocalDate.now(), null, "Some improvement");
            } catch (Exception e) {
                throw e.getCause(); // Unwrap InvocationTargetException
            }
        });

        // Verify
        assertInstanceOf(IllegalArgumentException.class, exception);
        assertTrue(exception.getMessage().contains("Strengths is required"),
                "Error message should mention that Strengths is required");
    }


}
