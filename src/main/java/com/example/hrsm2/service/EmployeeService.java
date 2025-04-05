package com.example.hrsm2.service;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.util.DatabaseDriver;

import java.util.Collections;
import java.util.List;
// Removed unused imports


public class EmployeeService {
    // Singleton instance
    private static EmployeeService instance;

    // Reference to the data access layer
    private final DatabaseDriver dbDriver;

    private EmployeeService() {
        // Initialize the database driver
        dbDriver = new DatabaseDriver();
    }

    /**
     * Gets the singleton instance of the EmployeeService.
     *
     * @return The single instance of EmployeeService.
     */
    public static synchronized EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }

    public List<Employee> getAllEmployees() {
        try {
            return dbDriver.getAllEmployees();
        } catch (Exception e) {
            System.err.println("Service Error: Failed to get all employees. " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return empty list on error
        }
    }

    public Employee getEmployeeById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        try {
            return dbDriver.getEmployeeById(id);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to get employee by ID " + id + ". " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds a new employee to the system.
     * The Employee object should have its UUID generated before calling this method.
     * Delegates the insertion operation to the DatabaseDriver.
     *
     * @param employee The Employee object to add (must have a valid UUID).
     * @return true if the employee was added successfully, false otherwise.
     */
    public boolean addEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            System.err.println("Service Error: Cannot add null employee or employee with null ID.");
            return false;
        }
        try {
            // Basic validation example (could add more complex business rules here)
            if (employee.getSalary() < 0) {
                System.err.println("Service Error: Salary cannot be negative.");
                return false;
            }
            return dbDriver.insertEmployee(employee);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to add employee ID " + employee.getId() + ". " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing employee's details in the system.
     * Delegates the update operation to the DatabaseDriver.
     *
     * @param employee The Employee object with updated information (must have the correct ID).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            System.err.println("Service Error: Cannot update null employee or employee with null ID.");
            return false;
        }
        try {
            // Basic validation example
            if (employee.getSalary() < 0) {
                System.err.println("Service Error: Salary cannot be negative.");
                return false;
            }
            return dbDriver.updateEmployee(employee);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to update employee ID " + employee.getId() + ". " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an employee from the system using their String ID (UUID).
     * Delegates the deletion operation to the DatabaseDriver.
     *
     * @param id The String UUID of the employee to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteEmployee(String id) {
        if (id == null || id.trim().isEmpty()) {
            System.err.println("Service Error: Cannot delete employee with null or empty ID.");
            return false;
        }
        try {
            return dbDriver.deleteEmployee(id);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to delete employee ID " + id + ". " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Employee> searchEmployees(String keyword) {
        // Service layer might cleanse the keyword, but for now, pass directly
        try {
            return dbDriver.searchEmployees(keyword);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to search employees with keyword '" + keyword + "'. " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Closes the underlying database connection.
     * Should be called when the application is shutting down.
     */
    public void closeDatabaseConnection() {
        System.out.println("EmployeeService requesting database connection closure.");
        dbDriver.closeConnection();
    }
}