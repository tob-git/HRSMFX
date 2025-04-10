package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.PerformanceEvaluation;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.PerformanceEvaluationService;
import com.example.hrsm2.service.UserService;

import java.time.LocalDate;
import java.util.List;

/**
 * Contains business logic for managing performance evaluations.
 */
public class PerformanceController {
    // Services
    private EmployeeService employeeService;
    private PerformanceEvaluationService evaluationService;
    private UserService userService;
    
    // Current logged-in user
    private User currentUser;
    

    public PerformanceController() {
        this.employeeService = EmployeeService.getInstance();
        this.evaluationService = PerformanceEvaluationService.getInstance();
        this.userService = UserService.getInstance();
        this.currentUser = userService.getCurrentUser();
    }
    
    /**
     * Gets all employees from the employee service.
     * 
     * @return a list of all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }
    
    /**
     * Gets all performance evaluations from the evaluation service.
     * 
     * @return a list of all performance evaluations
     */
    public List<PerformanceEvaluation> getAllEvaluations() {
        return evaluationService.getAllEvaluations();
    }
    
    /**
     * Get employee by ID
     * @param id Employee ID
     * @return Employee object if found, null otherwise
     */
    public Employee getEmployeeById(String id) {
        return employeeService.getEmployeeById(id);
    }
    
    /**
     * Adds a new performance evaluation.
     * 
     * @param employeeId the ID of the employee being evaluated
     * @param evaluationDate the date of the evaluation
     * @param rating the performance rating (1-5)
     * @param strengths the employee's strengths
     * @param improvement areas for improvement
     * @param comments additional comments
     * @return true if the evaluation was added successfully, false otherwise
     */
    public boolean addEvaluation(String employeeId, LocalDate evaluationDate, 
                               int rating, String strengths, 
                               String improvement, String comments) {
        try {
            validateEvaluationData(employeeId, evaluationDate, strengths, improvement);
            
            PerformanceEvaluation evaluation = new PerformanceEvaluation(
                employeeId,
                rating,
                strengths,
                improvement,
                comments,
                currentUser.getFullName()
            );
            
            evaluation.setEvaluationDate(evaluationDate);
            
            evaluationService.addEvaluation(evaluation);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Updates an existing performance evaluation.
     * 
     * @param evaluationId the ID of the evaluation to update
     * @param employeeId the ID of the employee being evaluated
     * @param evaluationDate the date of the evaluation
     * @param rating the performance rating (1-5)
     * @param strengths the employee's strengths
     * @param improvement areas for improvement
     * @param comments additional comments
     * @return true if the evaluation was updated successfully, false otherwise
     */
    public boolean updateEvaluation(String evaluationId, String employeeId, 
                                  LocalDate evaluationDate, int rating, 
                                  String strengths, String improvement, 
                                  String comments) {
        try {
            validateEvaluationData(employeeId, evaluationDate, strengths, improvement);
            
            PerformanceEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
            if (evaluation == null) {
                return false;
            }
            
            evaluation.setEmployeeId(employeeId);
            evaluation.setEvaluationDate(evaluationDate);
            evaluation.setPerformanceRating(rating);
            evaluation.setStrengths(strengths);
            evaluation.setAreasForImprovement(improvement);
            evaluation.setComments(comments);
            evaluation.setReviewedBy(currentUser.getFullName());
            
            evaluationService.updateEvaluation(evaluation);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a performance evaluation by ID.
     * 
     * @param evaluationId the ID of the evaluation to delete
     * @return true if the evaluation was deleted successfully
     */
    public boolean deleteEvaluation(String evaluationId) {
        try {
            evaluationService.deleteEvaluation(evaluationId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets a performance evaluation by ID.
     * 
     * @param evaluationId the ID of the evaluation to retrieve
     * @return the performance evaluation, or null if not found
     */
    public PerformanceEvaluation getEvaluationById(String evaluationId) {
        return evaluationService.getEvaluationById(evaluationId);
    }
    
    /**
     * Gets a description for a performance rating.
     * 
     * @param rating the numeric rating (1-5)
     * @return a text description of the rating
     */
    public String getRatingDescription(int rating) {
        return switch (rating) {
            case 1 -> "Poor";
            case 2 -> "Below Average";
            case 3 -> "Average";
            case 4 -> "Good";
            case 5 -> "Excellent";
            default -> "Not Rated";
        };
    }
    
    /**
     * Validates performance evaluation data.
     * 
     * @param employeeId the employee ID
     * @param evaluationDate the evaluation date
     * @param strengths the strengths text
     * @param improvement the areas for improvement text
     * @throws IllegalArgumentException if any validation fails
     */
    private void validateEvaluationData(String employeeId, LocalDate evaluationDate, 
                                      String strengths, String improvement) {
        StringBuilder errorMessage = new StringBuilder();
        
        if (employeeId == null || employeeId.trim().isEmpty()) {
            errorMessage.append("Employee is required.\n");
        } else if (employeeService.getEmployeeById(employeeId) == null) {
            errorMessage.append("Selected employee does not exist.\n");
        }
        
        if (evaluationDate == null) {
            errorMessage.append("Evaluation date is required.\n");
        }
        
        if (strengths == null || strengths.trim().isEmpty()) {
            errorMessage.append("Strengths is required.\n");
        }
        
        if (improvement == null || improvement.trim().isEmpty()) {
            errorMessage.append("Areas for improvement is required.\n");
        }
        
        if (errorMessage.length() > 0) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }
    
    /**
     * Gets the current user.
     * 
     * @return the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the current user.
     * 
     * @param user the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}