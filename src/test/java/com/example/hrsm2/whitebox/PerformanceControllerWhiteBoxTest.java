package com.example.hrsm2.whitebox;

import com.example.hrsm2.controller.PerformanceController;
import com.example.hrsm2.model.PerformanceEvaluation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White box tests for PerformanceController
 * These tests focus on internal paths, branch coverage, and decision logic
 */
public class PerformanceControllerWhiteBoxTest {
    
    private PerformanceController controller;
    
    @BeforeEach
    public void setUp() {
        controller = new PerformanceController();
    }
    
    /**
     * Test the validateEvaluationData method directly using reflection
     * This is white box testing since we're testing an internal private method
     */
    @Test
    public void testValidateEvaluationDataWithNullEmployee() throws Exception {
        // Use reflection to access private method
        Method validateMethod = PerformanceController.class.getDeclaredMethod(
            "validateEvaluationData", 
            String.class, LocalDate.class, String.class, String.class
        );
        validateMethod.setAccessible(true);
        
        // Test null employee ID case
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            validateMethod.invoke(controller, null, LocalDate.now(), "Strengths", "Improvements");
        });
        
        // The actual exception will be wrapped in an InvocationTargetException
        assertTrue(exception.getCause().getMessage().contains("Employee is required"));
    }
    
    /**
     * Test the validateEvaluationData method with null evaluation date
     */
    @Test
    public void testValidateEvaluationDataWithNullDate() throws Exception {
        Method validateMethod = PerformanceController.class.getDeclaredMethod(
            "validateEvaluationData", 
            String.class, LocalDate.class, String.class, String.class
        );
        validateMethod.setAccessible(true);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            validateMethod.invoke(controller, "emp123", null, "Strengths", "Improvements");
        });
        
        assertTrue(exception.getCause().getMessage().contains("Evaluation date is required"));
    }
    
    /**
     * Test multiple validation errors to cover the branch where multiple messages are added
     */
    @Test
    public void testValidateEvaluationDataWithMultipleErrors() throws Exception {
        Method validateMethod = PerformanceController.class.getDeclaredMethod(
            "validateEvaluationData", 
            String.class, LocalDate.class, String.class, String.class
        );
        validateMethod.setAccessible(true);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            validateMethod.invoke(controller, "", null, "", "");
        });
        
        String message = exception.getCause().getMessage();
        assertTrue(message.contains("Employee is required"));
        assertTrue(message.contains("Evaluation date is required"));
        assertTrue(message.contains("Strengths is required"));
        assertTrue(message.contains("Areas for improvement is required"));
    }
} 