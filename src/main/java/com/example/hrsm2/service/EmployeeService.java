package com.example.hrsm2.service;

import com.example.hrsm2.model.Employee;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeService {
    private Map<String, Employee> employees;
    private static EmployeeService instance;

    private EmployeeService() {
        employees = new HashMap<>();
    }

    public static EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    public Employee getEmployeeById(String id) {
        return employees.get(id);
    }

    public void addEmployee(Employee employee) {
        employees.put(employee.getId(), employee);
    }

    public void updateEmployee(Employee employee) {
        if (employees.containsKey(employee.getId())) {
            employees.put(employee.getId(), employee);
        }
    }

    public void deleteEmployee(String id) {
        employees.remove(id);
    }

    public List<Employee> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        List<Employee> results = new ArrayList<>();
        
        for (Employee employee : employees.values()) {
            if (employee.getFirstName().toLowerCase().contains(lowerKeyword) ||
                employee.getLastName().toLowerCase().contains(lowerKeyword) ||
                employee.getEmail().toLowerCase().contains(lowerKeyword) ||
                employee.getDepartment().toLowerCase().contains(lowerKeyword) ||
                employee.getJobTitle().toLowerCase().contains(lowerKeyword)) {
                results.add(employee);
            }
        }
        
        return results;
    }
} 