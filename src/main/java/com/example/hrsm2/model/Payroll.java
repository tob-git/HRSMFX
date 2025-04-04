package com.example.hrsm2.model;

import java.time.LocalDate;
import java.util.UUID;

public class Payroll {
    private String id;
    private String employeeId;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private double baseSalary;
    private double overtimePay;
    private double bonus;
    private double taxDeductions;
    private double otherDeductions;
    private double netSalary;
    private PayrollStatus status;

    public enum PayrollStatus {
        PENDING,
        PROCESSED,
        PAID
    }

    public Payroll() {
        this.id = UUID.randomUUID().toString();
        this.status = PayrollStatus.PENDING;
    }

    public Payroll(String employeeId, LocalDate payPeriodStart, LocalDate payPeriodEnd, 
                  double baseSalary) {
        this();
        this.employeeId = employeeId;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.baseSalary = baseSalary;
        calculateNetSalary();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public LocalDate getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(LocalDate payPeriodStart) { this.payPeriodStart = payPeriodStart; }
    public LocalDate getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(LocalDate payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }
    public double getBaseSalary() { return baseSalary; }
    
    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
        calculateNetSalary();
    }
    
    public double getOvertimePay() { return overtimePay; }
    
    public void setOvertimePay(double overtimePay) {
        this.overtimePay = overtimePay;
        calculateNetSalary();
    }
    
    public double getBonus() { return bonus; }
    
    public void setBonus(double bonus) {
        this.bonus = bonus;
        calculateNetSalary();
    }
    
    public double getTaxDeductions() { return taxDeductions; }
    
    public void setTaxDeductions(double taxDeductions) {
        this.taxDeductions = taxDeductions;
        calculateNetSalary();
    }
    
    public double getOtherDeductions() { return otherDeductions; }
    
    public void setOtherDeductions(double otherDeductions) {
        this.otherDeductions = otherDeductions;
        calculateNetSalary();
    }
    
    public double getNetSalary() { return netSalary; }
    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }

    // Calculate net salary
    public void calculateNetSalary() {
        double grossSalary = baseSalary + overtimePay + bonus;
        this.netSalary = grossSalary - taxDeductions - otherDeductions;
    }

    // Get total earnings (before deductions)
    public double getTotalEarnings() {
        return baseSalary + overtimePay + bonus;
    }

    // Get total deductions
    public double getTotalDeductions() {
        return taxDeductions + otherDeductions;
    }
} 