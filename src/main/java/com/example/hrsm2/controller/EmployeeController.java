package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class EmployeeController implements Initializable {
    @FXML
    private TableView<Employee> employeeTable;
    
    @FXML
    private TableColumn<Employee, String> idColumn;
    
    @FXML
    private TableColumn<Employee, String> firstNameColumn;
    
    @FXML
    private TableColumn<Employee, String> lastNameColumn;
    
    @FXML
    private TableColumn<Employee, String> emailColumn;
    
    @FXML
    private TableColumn<Employee, String> phoneColumn;
    
    @FXML
    private TableColumn<Employee, LocalDate> hireDateColumn;
    
    @FXML
    private TableColumn<Employee, String> departmentColumn;
    
    @FXML
    private TableColumn<Employee, String> jobTitleColumn;
    
    @FXML
    private TableColumn<Employee, Double> salaryColumn;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private DatePicker hireDatePicker;
    
    @FXML
    private TextField departmentField;
    
    @FXML
    private TextField jobTitleField;
    
    @FXML
    private TextField salaryField;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button clearButton;
    
    private final EmployeeService employeeService = EmployeeService.getInstance();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployees;
    private Employee selectedEmployee;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        hireDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        jobTitleColumn.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        
        // Setup date picker
        setupDatePicker(hireDatePicker);
        
        // Set today's date as default
        hireDatePicker.setValue(LocalDate.now());
        
        // Setup table selection listener
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEmployee = newSelection;
                showEmployeeDetails(selectedEmployee);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        
        // Initialize button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        
        // Load initial employee data
        refreshEmployeeList();
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchEmployees();
        });
    }
    
    private void setupDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
    }
    
    @FXML
    private void handleAddEmployee() {
        try {
            // Validate input data
            if (!validateInputs()) {
                return;
            }
            
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            LocalDate hireDate = hireDatePicker.getValue();
            String department = departmentField.getText();
            String jobTitle = jobTitleField.getText();
            double salary = Double.parseDouble(salaryField.getText());
            
            // Create new employee
            Employee newEmployee = new Employee(firstName, lastName, email, phone, hireDate, department, jobTitle, salary);
            
            // Add to service
            employeeService.addEmployee(newEmployee);
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully.");
            
            // Clear form and refresh list
            clearForm();
            refreshEmployeeList();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Salary must be a valid number.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add employee: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateEmployee() {
        if (selectedEmployee == null) {
            return;
        }
        
        try {
            // Validate input data
            if (!validateInputs()) {
                return;
            }
            
            // Update employee data
            selectedEmployee.setFirstName(firstNameField.getText());
            selectedEmployee.setLastName(lastNameField.getText());
            selectedEmployee.setEmail(emailField.getText());
            selectedEmployee.setPhone(phoneField.getText());
            selectedEmployee.setHireDate(hireDatePicker.getValue());
            selectedEmployee.setDepartment(departmentField.getText());
            selectedEmployee.setJobTitle(jobTitleField.getText());
            selectedEmployee.setSalary(Double.parseDouble(salaryField.getText()));
            
            // Update in service
            employeeService.updateEmployee(selectedEmployee);
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated successfully.");
            
            // Refresh the table
            refreshEmployeeList();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Salary must be a valid number.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update employee: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteEmployee() {
        if (selectedEmployee == null) {
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Employee");
        confirmAlert.setContentText("Are you sure you want to delete this employee?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete from service
                employeeService.deleteEmployee(selectedEmployee.getId());
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee deleted successfully.");
                
                // Clear form and refresh list
                clearForm();
                refreshEmployeeList();
                
                // Reset selection
                selectedEmployee = null;
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }
    
    @FXML
    private void handleClearForm() {
        clearForm();
        employeeTable.getSelectionModel().clearSelection();
        selectedEmployee = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML
    private void searchEmployees() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        
        if (filteredEmployees == null) {
            filteredEmployees = new FilteredList<>(employeeList);
        }
        
        Predicate<Employee> predicate = employee -> {
            if (searchTerm.isEmpty()) {
                return true;
            }
            
            return employee.getFirstName().toLowerCase().contains(searchTerm) ||
                   employee.getLastName().toLowerCase().contains(searchTerm) ||
                   employee.getEmail().toLowerCase().contains(searchTerm) ||
                   employee.getDepartment().toLowerCase().contains(searchTerm) ||
                   employee.getJobTitle().toLowerCase().contains(searchTerm);
        };
        
        filteredEmployees.setPredicate(predicate);
        employeeTable.setItems(filteredEmployees);
    }
    
    private void refreshEmployeeList() {
        employeeList.clear();
        employeeList.addAll(employeeService.getAllEmployees());
        
        if (filteredEmployees == null) {
            filteredEmployees = new FilteredList<>(employeeList);
            employeeTable.setItems(filteredEmployees);
        } else {
            // Reset search
            searchField.clear();
            filteredEmployees.setPredicate(employee -> true);
        }
    }
    
    private void showEmployeeDetails(Employee employee) {
        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        emailField.setText(employee.getEmail());
        phoneField.setText(employee.getPhone());
        hireDatePicker.setValue(employee.getHireDate());
        departmentField.setText(employee.getDepartment());
        jobTitleField.setText(employee.getJobTitle());
        salaryField.setText(String.valueOf(employee.getSalary()));
    }
    
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        hireDatePicker.setValue(LocalDate.now());
        departmentField.clear();
        jobTitleField.clear();
        salaryField.clear();
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("First name is required.\n");
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("Last name is required.\n");
        }
        
        if (emailField.getText().trim().isEmpty()) {
            errorMessage.append("Email is required.\n");
        } else if (!emailField.getText().contains("@")) {
            errorMessage.append("Invalid email format.\n");
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            errorMessage.append("Phone number is required.\n");
        }
        
        if (hireDatePicker.getValue() == null) {
            errorMessage.append("Hire date is required.\n");
        }
        
        if (departmentField.getText().trim().isEmpty()) {
            errorMessage.append("Department is required.\n");
        }
        
        if (jobTitleField.getText().trim().isEmpty()) {
            errorMessage.append("Job title is required.\n");
        }
        
        if (salaryField.getText().trim().isEmpty()) {
            errorMessage.append("Salary is required.\n");
        } else {
            try {
                double salary = Double.parseDouble(salaryField.getText().trim());
                if (salary <= 0) {
                    errorMessage.append("Salary must be greater than zero.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Salary must be a valid number.\n");
            }
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 