package com.example.hrsm2.service;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.Payroll;
import com.example.hrsm2.util.DatabaseDriver;

import java.time.LocalDate;
import java.util.ArrayList;
// import java.util.HashMap; // Remove HashMap import
import java.util.List;
// import java.util.Map; // Remove Map import
// import java.util.stream.Collectors; // No longer needed for filtering in memory

public class PayrollService {
    // Remove the in-memory map
    // private Map<String, Payroll> payrolls;

    private static PayrollService instance;
    private final EmployeeService employeeService;
    private final DatabaseDriver databaseDriver; // Add DatabaseDriver instance

    private PayrollService() {
        // Remove map initialization
        // payrolls = new HashMap<>();
        employeeService = EmployeeService.getInstance();
        databaseDriver = DatabaseDriver.getInstance(); // Get DatabaseDriver instance
    }

    public static PayrollService getInstance() {
        if (instance == null) {
            instance = new PayrollService();
        }
        return instance;
    }

    // --- Methods modified to use DatabaseDriver ---

    public List<Payroll> getAllPayrolls() {
        // Retrieve from database instead of map
        return databaseDriver.getAllPayrolls();
    }

    public Payroll getPayrollById(String id) {
        // Retrieve from database
        return databaseDriver.getPayrollById(id);
    }

    public List<Payroll> getPayrollsByEmployeeId(String employeeId) {
        // Retrieve from database
        return databaseDriver.getPayrollsByEmployeeId(employeeId);
    }

    public Payroll generatePayroll(String employeeId, LocalDate payPeriodStart, LocalDate payPeriodEnd) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            System.err.println("Cannot generate payroll: Employee not found with ID " + employeeId);
            return null;
        }

        // Calculate monthly salary (assuming employee.salary is annual)
        double monthlySalary = employee.getSalary() / 12;

        // Create a new payroll entry (ID is generated in constructor)
        Payroll payroll = new Payroll(employeeId, payPeriodStart, payPeriodEnd, monthlySalary);

        // Apply default deductions (example: 20% tax) - Keep this logic or adjust as needed
        double taxDeduction = monthlySalary * 0.2;
        payroll.setTaxDeductions(taxDeduction);

        // Recalculate net salary
        payroll.calculateNetSalary();

        // Save to database instead of map
        boolean success = databaseDriver.insertPayroll(payroll);

        // Return the payroll object if saved successfully, otherwise null
        return success ? payroll : null;
    }

    public boolean processPayroll(String id) {
        // Fetch from database
        Payroll payroll = databaseDriver.getPayrollById(id);

        if (payroll == null) {
            System.err.println("Cannot process payroll: Payroll not found with ID " + id);
            return false;
        }
        if (payroll.getStatus() != Payroll.PayrollStatus.PENDING) {
            System.err.println("Cannot process payroll: Payroll ID " + id + " is not in PENDING status (current: " + payroll.getStatus() + ")");
            return false; // Can only process pending payrolls
        }

        // Update status to processed
        payroll.setStatus(Payroll.PayrollStatus.PROCESSED);

        // Update in database
        return databaseDriver.updatePayroll(payroll);
    }

    public boolean markPayrollAsPaid(String id) {
        // Fetch from database
        Payroll payroll = databaseDriver.getPayrollById(id);

        if (payroll == null) {
            System.err.println("Cannot mark as paid: Payroll not found with ID " + id);
            return false;
        }
        if (payroll.getStatus() != Payroll.PayrollStatus.PROCESSED) {
            System.err.println("Cannot mark as paid: Payroll ID " + id + " is not in PROCESSED status (current: " + payroll.getStatus() + ")");
            return false; // Can only mark processed payrolls as paid
        }

        // Update status to paid
        payroll.setStatus(Payroll.PayrollStatus.PAID);

        // Update in database
        return databaseDriver.updatePayroll(payroll);
    }

    /**
     * Updates the details of an existing payroll record in the database.
     * Note: Status changes should generally go through processPayroll or markPayrollAsPaid.
     * This method is primarily for updating salary components (bonus, overtime, deductions).
     *
     * @param payroll The Payroll object with updated details. The ID must match an existing record.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePayroll(Payroll payroll) {
        if (payroll == null || payroll.getId() == null) {
            return false;
        }
        // Ensure net salary is correct before saving
        payroll.calculateNetSalary();
        // Update in database
        return databaseDriver.updatePayroll(payroll);
    }

    /**
     * Deletes a payroll record from the database.
     * Use with caution.
     *
     * @param id The ID of the payroll record to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deletePayroll(String id) {
        // Delete from database
        return databaseDriver.deletePayroll(id);
    }

    /**
     * Generates payroll records for all employees for the given period and saves them to the database.
     *
     * @param payPeriodStart The start date of the pay period.
     * @param payPeriodEnd   The end date of the pay period.
     * @return A list of the generated Payroll objects that were successfully saved.
     */
    public List<Payroll> generatePayrollsForAllEmployees(LocalDate payPeriodStart, LocalDate payPeriodEnd) {
        List<Employee> employees = employeeService.getAllEmployees();
        List<Payroll> generatedPayrolls = new ArrayList<>();

        if (employees.isEmpty()) {
            System.out.println("No employees found to generate payroll for.");
            return generatedPayrolls; // Return empty list
        }

        System.out.println("Generating payrolls for " + employees.size() + " employees...");
        for (Employee employee : employees) {
            // generatePayroll now handles creation AND saving to DB
            Payroll payroll = generatePayroll(employee.getId(), payPeriodStart, payPeriodEnd);
            if (payroll != null) {
                // Optionally update with default overtime/bonus if logic exists
                // payroll.setOvertimePay(...);
                // payroll.setBonus(...);
                // payroll.calculateNetSalary(); // Recalculate if defaults were added
                // databaseDriver.updatePayroll(payroll); // Update again if defaults added *after* insert

                generatedPayrolls.add(payroll);
                System.out.println("Successfully generated and saved payroll for employee: " + employee.getFullName() + " (Payroll ID: " + payroll.getId() + ")");
            } else {
                System.err.println("Failed to generate or save payroll for employee: " + employee.getFullName() + " (ID: " + employee.getId() + ")");
            }
        }
        System.out.println("Finished generating payrolls. " + generatedPayrolls.size() + " successful.");
        return generatedPayrolls;
    }
}