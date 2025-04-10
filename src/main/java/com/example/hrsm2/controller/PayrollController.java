package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.Payroll;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.PayrollService;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller class for Payroll management.
 * This class handles business logic and delegates data operations to PayrollService.
 * It's designed for unit testing without UI dependencies.
 */
public class PayrollController {

    // Service layer instances
    private final EmployeeService employeeService;
    private final PayrollService payrollService;

    /**
     * Default constructor using the singleton instances of services
     */
    public PayrollController() {
        this.employeeService = EmployeeService.getInstance();
        this.payrollService = PayrollService.getInstance();
    }

    /**
     * Constructor for dependency injection, mainly used for unit testing
     * @param employeeService The employee service to use
     * @param payrollService The payroll service to use
     */
    public PayrollController(EmployeeService employeeService, PayrollService payrollService) {
        this.employeeService = employeeService;
        this.payrollService = payrollService;
    }

    /**
     * Get all payrolls from the system
     * @return List of all payrolls
     */
    public List<Payroll> getAllPayrolls() {
        return payrollService.getAllPayrolls();
    }

    /**
     * Get all employees from the system
     * @return List of all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    /**
     * Get an employee by ID
     * @param employeeId The employee ID
     * @return The employee if found, null otherwise
     */
    public Employee getEmployeeById(String employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }

    /**
     * Generate a payroll for a specific employee for a given period
     * @param employeeId The employee ID
     * @param startDate The start date of the pay period
     * @param endDate The end date of the pay period
     * @return The generated payroll object
     */
    public Payroll generatePayroll(String employeeId, LocalDate startDate, LocalDate endDate) {
        return payrollService.generatePayroll(employeeId, startDate, endDate);
    }

    /**
     * Generate payrolls for all employees for a given period
     * @param startDate The start date of the pay period
     * @param endDate The end date of the pay period
     */
    public void generatePayrollsForAllEmployees(LocalDate startDate, LocalDate endDate) {
        payrollService.generatePayrollsForAllEmployees(startDate, endDate);
    }

    /**
     * Update an existing payroll
     * @param payroll The payroll object with updated values
     * @return true if successful, false otherwise
     */
    public boolean updatePayroll(Payroll payroll) {
        return payrollService.updatePayroll(payroll);
    }

    /**
     * Process a payroll (change status from PENDING to PROCESSED)
     * @param payrollId The ID of the payroll to process
     * @return true if successful, false otherwise
     */
    public boolean processPayroll(String payrollId) {
        return payrollService.processPayroll(payrollId);
    }

    /**
     * Mark a payroll as paid (change status from PROCESSED to PAID)
     * @param payrollId The ID of the payroll to mark as paid
     * @return true if successful, false otherwise
     */
    public boolean markPayrollAsPaid(String payrollId) {
        return payrollService.markPayrollAsPaid(payrollId);
    }

    /**
     * Calculate the monthly salary for an employee
     * @param employee The employee object
     * @return The calculated monthly salary
     */
    public double calculateMonthlySalary(Employee employee) {
        if (employee == null) {
            return 0.0;
        }
        return employee.getSalary() / 12.0;
    }

    /**
     * Calculate net salary based on inputs
     * @param baseSalary The base salary
     * @param overtime The overtime pay
     * @param bonus The bonus amount
     * @param taxDeductions Tax deductions
     * @param otherDeductions Other deductions
     * @param startDate The start date of the period
     * @param endDate The end date of the period
     * @return The calculated net salary
     */
    public double calculateNetSalary(double baseSalary, double overtime, double bonus, 
            double taxDeductions, double otherDeductions, LocalDate startDate, LocalDate endDate) {
        
        double netSalary = 0.0;
        if (startDate != null && endDate != null && !endDate.isBefore(startDate)) {
            // Calculate days in the period as a proportion of a month (assuming 30 days)
            double daysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));
            double monthRatio = daysInPeriod / 30.0;
            
            netSalary = (baseSalary + overtime + bonus - taxDeductions - otherDeductions) * monthRatio;
        }
        return netSalary;
    }
    
    /**
     * Shutdown method to clean up resources
     */
    public void shutdown() {
        // If PayrollService doesn't have a shutdown method, we can just log or do nothing
        // payrollService.closeDatabaseConnection();
    }
} 