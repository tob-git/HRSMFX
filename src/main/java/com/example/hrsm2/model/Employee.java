package com.example.hrsm2.model;

import java.time.LocalDate;
import java.util.UUID; // Import UUID

public class Employee {
    private String id; // Changed from int to String (UUID)
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private String department;
    private String jobTitle;
    private double salary;
    private int availableLeave; // Example field, not in current DB schema

    public Employee() {
        this.id = UUID.randomUUID().toString(); // Generate UUID here
        this.availableLeave = 20; // Default value
    }

    public Employee(String firstName, String lastName, String email, String phone,
                    LocalDate hireDate, String department, String jobTitle, double salary) {
        this(); // Call default constructor to generate UUID
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.department = department;
        this.jobTitle = jobTitle;
        this.salary = salary;
    }

    /**
     * Constructor used primarily for loading an Employee object from the database,
     * where the ID is already known.
     *
     * @param id         The existing UUID String of the employee.
     * @param firstName  Employee's first name.
     * @param lastName   Employee's last name.
     * @param email      Employee's email address.
     * @param phone      Employee's phone number.
     * @param hireDate   Date the employee was hired.
     * @param department Department the employee belongs to.
     * @param jobTitle   Employee's job title.
     * @param salary     Employee's salary.
     */
    public Employee(String id, String firstName, String lastName, String email, String phone,
                    LocalDate hireDate, String department, String jobTitle, double salary) {
        this.id = id; // Use provided ID
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.hireDate = hireDate;
        this.department = department;
        this.jobTitle = jobTitle;
        this.salary = salary;
        this.availableLeave = 20; // Default or load if available in DB later
    }

    // --- Getters and Setters ---

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

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", hireDate=" + hireDate +
                ", department='" + department + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", salary=" + salary +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        // Primarily equality is based on the unique ID
        return id != null ? id.equals(employee.id) : employee.id == null;
    }

    @Override
    public int hashCode() {
        // Hash code based on the unique ID
        return id != null ? id.hashCode() : 0;
    }
}