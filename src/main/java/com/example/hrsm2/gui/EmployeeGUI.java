package com.example.hrsm2.gui;

import com.example.hrsm2.controller.EmployeeController;
import com.example.hrsm2.model.Employee;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * GUI class for Employee management.
 * This class handles all UI interactions and delegates business logic to EmployeeController.
 */
public class EmployeeGUI implements Initializable {

    // --- FXML Table and Columns ---
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> idColumn;
    @FXML private TableColumn<Employee, String> firstNameColumn;
    @FXML private TableColumn<Employee, String> lastNameColumn;
    @FXML private TableColumn<Employee, String> emailColumn;
    @FXML private TableColumn<Employee, String> phoneColumn;
    @FXML private TableColumn<Employee, LocalDate> hireDateColumn;
    @FXML private TableColumn<Employee, String> departmentColumn;
    @FXML private TableColumn<Employee, String> jobTitleColumn;
    @FXML private TableColumn<Employee, Double> salaryColumn;

    // --- FXML Form Fields ---
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField departmentField;
    @FXML private TextField jobTitleField;
    @FXML private TextField salaryField;
    @FXML private TextField searchField;

    // --- FXML Buttons ---
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // Controller for business logic
    private final EmployeeController employeeController = new EmployeeController();

    // Observable list to back the TableView
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    // Reference to the currently selected employee in the table
    private Employee selectedEmployee;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupDatePicker(hireDatePicker);
        setupTableSelectionListener();
        setupSearchFieldListener();

        // Set the items for the table view
        employeeTable.setItems(employeeList);

        // Initial button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        // Set default date
        hireDatePicker.setValue(LocalDate.now());

        // Load initial data
        refreshEmployeeList();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        hireDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        jobTitleColumn.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

        // Optional: Format Salary column as currency
        salaryColumn.setCellFactory(tc -> new TableCell<Employee, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", price));
                }
            }
        });

        // Optional: Format Date column
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        hireDateColumn.setCellFactory(column -> new TableCell<Employee, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });
    }

    private void setupDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (Exception e) {
                        System.err.println("Invalid date format entered: " + string);
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
    }

    private void setupTableSelectionListener() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedEmployee = newSelection;
            if (newSelection != null) {
                showEmployeeDetails(newSelection);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                clearForm();
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchEmployees();
        });
    }

    @FXML
    public void handleAddEmployee() {
        if (!validateInputs()) {
            return;
        }

        try {
            Employee employee = createEmployeeFromFields();
            boolean success = employeeController.addEmployee(employee);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully.");
                clearForm();
                refreshEmployeeList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add employee. Possible duplicate ID/Email or database issue.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Application Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpdateEmployee() {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an employee to update.");
            return;
        }
        
        if (!validateInputs()) {
            return;
        }

        try {
            updateEmployeeFromFields(selectedEmployee);
            boolean success = employeeController.updateEmployee(selectedEmployee);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated successfully.");
                clearForm();
                refreshEmployeeList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update employee. Possible duplicate email or database issue.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Application Error", "An unexpected error occurred during update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteEmployee() {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an employee to delete.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Employee");
        confirmAlert.setContentText("Are you sure you want to delete " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = employeeController.deleteEmployee(selectedEmployee.getId());

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Employee deleted successfully.");
                        clearForm();
                        refreshEmployeeList();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete employee. Employee might not exist or database error occurred.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Application Error", "An unexpected error occurred during deletion: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void handleClearForm() {
        clearForm();
        employeeTable.getSelectionModel().clearSelection();
        searchField.clear();
        refreshEmployeeList();
    }

    @FXML
    public void searchEmployees() {
        String searchTerm = searchField.getText();
        try {
            List<Employee> results = employeeController.searchEmployees(searchTerm);
            employeeList.setAll(results);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "An error occurred during search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshEmployeeList() {
        try {
            List<Employee> employees = employeeController.getAllEmployees();
            Platform.runLater(() -> {
                employeeList.setAll(employees);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load employee data: " + e.getMessage());
                employeeList.clear();
            });
            e.printStackTrace();
        }
    }

    private void showEmployeeDetails(Employee employee) {
        if (employee == null) {
            clearForm();
            return;
        }

        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        emailField.setText(employee.getEmail());
        phoneField.setText(employee.getPhone());
        hireDatePicker.setValue(employee.getHireDate());
        departmentField.setText(employee.getDepartment());
        jobTitleField.setText(employee.getJobTitle());
        salaryField.setText(String.format("%.2f", employee.getSalary()));
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

    private Employee createEmployeeFromFields() {
        return new Employee(
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            hireDatePicker.getValue(),
            departmentField.getText().trim(),
            jobTitleField.getText().trim(),
            Double.parseDouble(salaryField.getText().trim())
        );
    }

    private void updateEmployeeFromFields(Employee employee) {
        employee.setFirstName(firstNameField.getText().trim());
        employee.setLastName(lastNameField.getText().trim());
        employee.setEmail(emailField.getText().trim());
        employee.setPhone(phoneField.getText().trim());
        employee.setHireDate(hireDatePicker.getValue());
        employee.setDepartment(departmentField.getText().trim());
        employee.setJobTitle(jobTitleField.getText().trim());
        employee.setSalary(Double.parseDouble(salaryField.getText().trim()));
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        // --- Field Validations ---
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("First name is required.\n");
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("Last name is required.\n");
        }
        // Basic email format check
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            errorMessage.append("Email is required.\n");
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            errorMessage.append("Invalid email format.\n");
        }
        // Basic phone check
        String phone = phoneField.getText();
        if (phone == null || phone.trim().isEmpty()) {
            errorMessage.append("Phone number is required.\n");
        } else if (!phone.matches("^[\\d\\s()+-]*$")) {
            errorMessage.append("Invalid phone number format (only digits, spaces, +, -, () allowed).\n");
        }

        if (hireDatePicker.getValue() == null) {
            errorMessage.append("Hire date is required.\n");
        } else if (hireDatePicker.getValue().isAfter(LocalDate.now())) {
            errorMessage.append("Hire date cannot be in the future.\n");
        }

        if (departmentField.getText() == null || departmentField.getText().trim().isEmpty()) {
            errorMessage.append("Department is required.\n");
        }
        if (jobTitleField.getText() == null || jobTitleField.getText().trim().isEmpty()) {
            errorMessage.append("Job title is required.\n");
        }
        // Salary validation
        String salaryStr = salaryField.getText();
        if (salaryStr == null || salaryStr.trim().isEmpty()) {
            errorMessage.append("Salary is required.\n");
        } else {
            try {
                double salary = Double.parseDouble(salaryStr.trim());
                if (salary <= 0) {
                    errorMessage.append("Salary must be a positive number.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Salary must be a valid number (e.g., 50000.00).\n");
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

    // Called when the app shuts down
    public void shutdown() {
        employeeController.shutdown();
    }
} 