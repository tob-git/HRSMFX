package com.example.hrsm2.model;

import java.time.LocalDate;
import java.util.UUID;

public class Employee {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private String department;
    private String jobTitle;
    private double salary;
    private int availableLeave;

    public Employee() {
        this.id = UUID.randomUUID().toString();
        this.availableLeave = 20; // Default available leave days
    }

    public Employee(String firstName, String lastName, String email, String phone, 
                   LocalDate hireDate, String department, String jobTitle, double salary) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.department = department;
        this.jobTitle = jobTitle;
        this.salary = salary;
    }

    // Constructor with explicit ID (for loading from storage)
    public Employee(String id, String firstName, String lastName, String email, String phone, 
                   LocalDate hireDate, String department, String jobTitle, double salary) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.department = department;
        this.jobTitle = jobTitle;
        this.salary = salary;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public int getAvailableLeave() {
        return availableLeave;
    }

    public void setAvailableLeave(int availableLeave) {
        this.availableLeave = availableLeave;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
} 