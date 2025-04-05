package com.example.hrsm2.service;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.util.DatabaseDriver;

import java.util.Collections;
import java.util.List;
import java.util.UUID; // Keep UUID for potential ID generation if needed elsewhere

public class EmployeeService {
    // Singleton instance
    private static EmployeeService instance;

    // Reference to the SINGLETON data access layer instance
    private final DatabaseDriver dbDriver;

    // Private constructor to enforce Singleton pattern
    private EmployeeService() {
        // *** MODIFIED HERE: Use the Singleton instance of DatabaseDriver ***
        dbDriver = DatabaseDriver.getInstance();
    }

    /**
     * Gets the singleton instance of the EmployeeService.
     * Ensures only one instance of this service exists.
     *
     * @return The single instance of EmployeeService.
     */
    public static synchronized EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }

    /**
     * Retrieves all employees from the database.
     * Includes basic error handling.
     *
     * @return A List of all Employee objects, or an empty list if an error occurs.
     */
    public List<Employee> getAllEmployees() {
        try {
            return dbDriver.getAllEmployees();
        } catch (Exception e) {
            System.err.println("Service Error: Failed to get all employees. " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return empty list on error
        }
    }

    /**
     * Retrieves a specific employee by their String ID (UUID).
     * Includes basic error handling.
     *
     * @param id The String UUID of the employee to retrieve.
     * @return The Employee object if found, null otherwise or if an error occurs.
     */
    public Employee getEmployeeById(String id) {
        // Basic validation for ID format could be added here if needed
        if (id == null || id.trim().isEmpty()) {
            System.err.println("Service Info: getEmployeeById called with null or empty ID.");
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
     * Ensures the Employee object has a valid UUID before attempting insertion.
     * Delegates the insertion operation to the DatabaseDriver.
     *
     * @param employee The Employee object to add. If ID is null, a new UUID will be generated.
     * @return true if the employee was added successfully, false otherwise.
     */
    public boolean addEmployee(Employee employee) {
        if (employee == null) {
            System.err.println("Service Error: Cannot add null employee.");
            return false;
        }
        // Ensure employee has a UUID before inserting
        if (employee.getId() == null || employee.getId().trim().isEmpty()) {
            String newId = UUID.randomUUID().toString();
            System.out.println("Service Info: Generating new UUID for employee: " + newId);
            employee.setId(newId);
        }

        try {
            // Basic business rule validation (example)
            if (employee.getSalary() < 0) {
                System.err.println("Service Error: Salary cannot be negative for employee ID " + employee.getId());
                return false;
            }
            // Add more validation: check email format, phone format, etc.
            // if (!isValidEmail(employee.getEmail())) { ... return false; }

            return dbDriver.insertEmployee(employee);
        } catch (Exception e) {
            // Log the specific employee ID if available
            String employeeId = (employee != null && employee.getId() != null) ? employee.getId() : "N/A";
            System.err.println("Service Error: Failed to add employee ID " + employeeId + ". " + e.getMessage());
            // Check for specific DB errors (like unique constraint violation)
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: Employee.email")) {
                System.err.println("Service Hint: The email address might already be in use.");
                // Optionally re-throw a custom exception or return a specific error code/message
            }
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
        if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            System.err.println("Service Error: Cannot update null employee or employee with null/empty ID.");
            return false;
        }
        try {
            // Basic business rule validation (example)
            if (employee.getSalary() < 0) {
                System.err.println("Service Error: Salary cannot be negative for employee ID " + employee.getId());
                return false;
            }
            // Add more validation as needed

            return dbDriver.updateEmployee(employee);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to update employee ID " + employee.getId() + ". " + e.getMessage());
            // Check for specific DB errors (like unique constraint violation on email update)
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed: Employee.email")) {
                System.err.println("Service Hint: The updated email address might already be in use by another employee.");
                // Optionally re-throw a custom exception or return a specific error code/message
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an employee from the system using their String ID (UUID).
     * Delegates the deletion operation to the DatabaseDriver.
     * Consider adding checks here (e.g., cannot delete employee with active assignments/payroll).
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
            // Potential Business Logic: Check if employee can be deleted
            // e.g., boolean hasActiveLeave = leaveRequestService.hasActiveLeave(id);
            // if (hasActiveLeave) {
            //     System.err.println("Service Info: Cannot delete employee " + id + " due to active leave requests.");
            //     return false;
            // }

            return dbDriver.deleteEmployee(id);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to delete employee ID " + id + ". " + e.getMessage());
            // DB Foreign Key constraints should handle related data deletion if set up with CASCADE,
            // otherwise, deletion might fail here if related records exist.
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Searches for employees based on a keyword matching various fields.
     * Delegates the search operation to the DatabaseDriver.
     *
     * @param keyword The search term (can be null or empty to return all employees).
     * @return A List of matching Employee objects, or an empty list if an error occurs.
     */
    public List<Employee> searchEmployees(String keyword) {
        // Service layer might cleanse/validate the keyword, but for now, pass directly
        String sanitizedKeyword = (keyword == null) ? "" : keyword.trim(); // Example sanitization
        try {
            return dbDriver.searchEmployees(sanitizedKeyword);
        } catch (Exception e) {
            System.err.println("Service Error: Failed to search employees with keyword '" + sanitizedKeyword + "'. " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Closes the underlying database connection via the DatabaseDriver instance.
     * This method might not be strictly necessary in the service layer itself,
     * as connection management could be handled globally at application shutdown.
     * However, keeping it allows explicit closure request if needed.
     */
    public void closeDatabaseConnection() {
        // This method is less critical now that DatabaseDriver is a Singleton,
        // as only the Singleton's close method needs to be called once at app shutdown.
        // Calling it multiple times via different services won't hurt if DatabaseDriver handles it gracefully.
        System.out.println("EmployeeService requesting database connection closure (via Singleton Driver).");
        dbDriver.closeConnection(); // Delegates to the single driver instance's close method
    }
}