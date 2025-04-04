package com.example.hrsm2.service;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.Payroll;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayrollService {
    private Map<String, Payroll> payrolls;
    private static PayrollService instance;
    private final EmployeeService employeeService;

    private PayrollService() {
        payrolls = new HashMap<>();
        employeeService = EmployeeService.getInstance();
    }

    public static PayrollService getInstance() {
        if (instance == null) {
            instance = new PayrollService();
        }
        return instance;
    }

    public List<Payroll> getAllPayrolls() {
        return new ArrayList<>(payrolls.values());
    }

    public Payroll getPayrollById(String id) {
        return payrolls.get(id);
    }

    public List<Payroll> getPayrollsByEmployeeId(String employeeId) {
        return payrolls.values()
                .stream()
                .filter(payroll -> payroll.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public Payroll generatePayroll(String employeeId, LocalDate payPeriodStart, LocalDate payPeriodEnd) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            return null;
        }
        
        // Calculate monthly salary (assuming employee.salary is annual)
        double monthlySalary = employee.getSalary() / 12;
        
        // Create a new payroll entry
        Payroll payroll = new Payroll(employeeId, payPeriodStart, payPeriodEnd, monthlySalary);
        
        // Apply default deductions (example: 20% tax)
        double taxDeduction = monthlySalary * 0.2;
        payroll.setTaxDeductions(taxDeduction);
        
        // Recalculate net salary
        payroll.calculateNetSalary();
        
        // Add to payrolls map
        payrolls.put(payroll.getId(), payroll);
        
        return payroll;
    }

    public boolean processPayroll(String id) {
        Payroll payroll = payrolls.get(id);
        if (payroll == null || payroll.getStatus() != Payroll.PayrollStatus.PENDING) {
            return false;
        }
        
        // Update status to processed
        payroll.setStatus(Payroll.PayrollStatus.PROCESSED);
        return true;
    }

    public boolean markPayrollAsPaid(String id) {
        Payroll payroll = payrolls.get(id);
        if (payroll == null || payroll.getStatus() != Payroll.PayrollStatus.PROCESSED) {
            return false;
        }
        
        // Update status to paid
        payroll.setStatus(Payroll.PayrollStatus.PAID);
        return true;
    }

    public void updatePayroll(Payroll payroll) {
        if (payrolls.containsKey(payroll.getId())) {
            payrolls.put(payroll.getId(), payroll);
        }
    }

    public void deletePayroll(String id) {
        payrolls.remove(id);
    }
    
    // Method to generate payrolls for all employees
    public List<Payroll> generatePayrollsForAllEmployees(LocalDate payPeriodStart, LocalDate payPeriodEnd) {
        List<Employee> employees = employeeService.getAllEmployees();
        List<Payroll> generatedPayrolls = new ArrayList<>();
        
        for (Employee employee : employees) {
            Payroll payroll = generatePayroll(employee.getId(), payPeriodStart, payPeriodEnd);
            if (payroll != null) {
                generatedPayrolls.add(payroll);
            }
        }
        
        return generatedPayrolls;
    }
} 