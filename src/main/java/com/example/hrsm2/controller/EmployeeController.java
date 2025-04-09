package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.event.EmployeeEvent;
import com.example.hrsm2.event.EventManager;
import java.util.List;

/**
 * Controller class for Employee management.
 * This class handles business logic and delegates data operations to EmployeeService.
 * It's designed for unit testing without UI dependencies.
 */
public class EmployeeController {

    // Service layer instance
    private final EmployeeService employeeService;

    /**
     * Default constructor using the singleton instance of EmployeeService
     */
    public EmployeeController() {
        this.employeeService = EmployeeService.getInstance();
    }

    /**
     * Constructor for dependency injection, mainly used for unit testing
     * @param employeeService The employee service to use
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Add a new employee to the system
     * @param employee The employee to add
     * @return true if successful, false otherwise
     */
    public boolean addEmployee(Employee employee) {
        boolean success = employeeService.addEmployee(employee);
        if (success) {
            // Fire event to notify other controllers
            EventManager.getInstance().fireEvent(new EmployeeEvent(EmployeeEvent.EMPLOYEE_ADDED, employee));
        }
        return success;
    }

    /**
     * Update an existing employee
     * @param employee The employee with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateEmployee(Employee employee) {
        boolean success = employeeService.updateEmployee(employee);
        if (success) {
            // Fire event to notify other controllers
            EventManager.getInstance().fireEvent(new EmployeeEvent(EmployeeEvent.EMPLOYEE_UPDATED, employee));
        }
        return success;
    }

    /**
     * Delete an employee by ID
     * @param employeeId The ID of the employee to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteEmployee(String employeeId) {
        // Get the employee first so we can send it in the event if deletion is successful
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            return false;
        }
        
        boolean success = employeeService.deleteEmployee(employeeId);
        if (success) {
            // Fire event to notify other controllers
            EventManager.getInstance().fireEvent(new EmployeeEvent(EmployeeEvent.EMPLOYEE_DELETED, employee));
        }
        return success;
    }

    /**
     * Search for employees by search term
     * @param searchTerm The term to search for
     * @return List of matching employees
     */
    public List<Employee> searchEmployees(String searchTerm) {
        return employeeService.searchEmployees(searchTerm);
    }

    /**
     * Get all employees in the system
     * @return List of all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    /**
     * Get an employee by ID
     * @param employeeId The ID of the employee to retrieve
     * @return The employee if found, null otherwise
     */
    public Employee getEmployeeById(String employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }

    /**
     * Shutdown method to clean up resources
     */
    public void shutdown() {
        employeeService.closeDatabaseConnection();
    }
}