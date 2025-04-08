package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.event.EmployeeEvent;
import com.example.hrsm2.event.EventManager;
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

public class EmployeeController implements Initializable {

    // --- FXML Table and Columns ---
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> idColumn; // Display String UUID
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

    // Service layer instance
    private final EmployeeService employeeService = EmployeeService.getInstance();

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

        // Load initial data from the database
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
                    // Consider using NumberFormat for locale-specific currency formatting
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
                        // Handle parse exception if necessary, maybe show feedback
                        System.err.println("Invalid date format entered: " + string);
                        return null; // Or return current value, or show error
                    }
                } else {
                    return null;
                }
            }
        });
    }

    private void setupTableSelectionListener() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedEmployee = newSelection; // Keep track of the selected item
            if (newSelection != null) {
                showEmployeeDetails(newSelection); // Populate form with selected data
                updateButton.setDisable(false);   // Enable update/delete buttons
                deleteButton.setDisable(false);
            } else {
                // No selection or selection cleared
                clearForm();                     // Clear the form
                updateButton.setDisable(true);    // Disable update/delete buttons
                deleteButton.setDisable(true);
            }
        });
    }

    private void setupSearchFieldListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchEmployees(); // Perform search whenever text changes
        });
    }


    // --- FXML Action Handlers ---

    @FXML
    private void handleAddEmployee() {
        if (!validateInputs()) {
            return; // Validation failed, message shown by validateInputs
        }

        try {
            // Create a new Employee object (ID is generated in the constructor)
            Employee newEmployee = new Employee(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    hireDatePicker.getValue(),
                    departmentField.getText().trim(),
                    jobTitleField.getText().trim(),
                    Double.parseDouble(salaryField.getText().trim()) // Already validated number format
            );

            // Call service to add the employee to the database
            boolean success = employeeService.addEmployee(newEmployee);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully.");
                clearForm();             // Clear the input fields
                refreshEmployeeList();
                
                // Fire event to notify other controllers
                EventManager.getInstance().fireEvent(new EmployeeEvent(EmployeeEvent.EMPLOYEE_ADDED, newEmployee));
            } else {
                // Service/DB driver should log specifics, show generic error here
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add employee. Possible duplicate ID/Email or database issue. Check logs.");
            }

        } catch (NumberFormatException e) {
            // This should theoretically not happen if validateInputs is correct, but good to have
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid salary format.");
        } catch (Exception e) {
            // Catch unexpected errors during the process
            showAlert(Alert.AlertType.ERROR, "Application Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Log detailed error
        }
    }

    @FXML
    private void handleUpdateEmployee() {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an employee to update.");
            return;
        }
        
        if (!validateInputs()) {
            return; // Validation failed, message shown by validateInputs
        }

        try {
            // Update the selected employee with new values
            selectedEmployee.setFirstName(firstNameField.getText().trim());
            selectedEmployee.setLastName(lastNameField.getText().trim());
            selectedEmployee.setEmail(emailField.getText().trim());
            selectedEmployee.setPhone(phoneField.getText().trim());
            selectedEmployee.setHireDate(hireDatePicker.getValue());
            selectedEmployee.setDepartment(departmentField.getText().trim());
            selectedEmployee.setJobTitle(jobTitleField.getText().trim());
            selectedEmployee.setSalary(Double.parseDouble(salaryField.getText().trim()));

            // Call service to update the employee in the database
            boolean success = employeeService.updateEmployee(selectedEmployee);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated successfully.");
                clearForm();             // Clear the input fields
                refreshEmployeeList();
                
                // Fire event to notify other controllers
                EventManager.getInstance().fireEvent(new EmployeeEvent(EmployeeEvent.EMPLOYEE_UPDATED, selectedEmployee));
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update employee. Possible duplicate email or database issue. Check logs.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid salary format.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Application Error", "An unexpected error occurred during update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteEmployee() {
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
                    // Call service to delete the employee from the database
                    boolean success = employeeService.deleteEmployee(selectedEmployee.getId());

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Employee deleted successfully.");
                        clearForm();             // Clear the input fields
                        refreshEmployeeList();
                        
                        // Fire event to notify other controllers
                        EventManager.getInstance().fireEvent(new EmployeeEvent(EmployeeEvent.EMPLOYEE_DELETED, selectedEmployee));
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete employee. Employee might not exist or a database error occurred.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Application Error", "An unexpected error occurred during deletion: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        employeeTable.getSelectionModel().clearSelection(); // Explicitly clear selection
        searchField.clear(); // Optionally clear search field as well
        refreshEmployeeList(); // Optionally show all employees again
    }

    // --- Helper Methods ---

    /**
     * Calls the service to search for employees based on the text in the search field
     * and updates the table with the results. Runs on the FX Application Thread.
     */
    @FXML // Make accessible if called directly from FXML (though usually called by listener)
    private void searchEmployees() {
        String searchTerm = searchField.getText(); // Get current search term
        // Perform search in background thread if it could be long-running?
        // For now, assume quick enough for UI thread.
        try {
            List<Employee> results = employeeService.searchEmployees(searchTerm);
            employeeList.setAll(results); // Update the observable list bound to the table
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "An error occurred during search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshEmployeeList() {
        try {
            // Fetch data (potentially slow, consider background thread for large datasets)
            List<Employee> employeesFromDb = employeeService.getAllEmployees();

            // Update the UI on the FX Application Thread
            Platform.runLater(() -> {
                employeeList.setAll(employeesFromDb);
                System.out.println("Employee list refreshed. Displaying " + employeeList.size() + " employees."); // Debug log
                // Do NOT clear selection here, as refresh might be called after update
                // table selection listener handles clearing form if selection is lost.
            });
        } catch (Exception e) {
            // Show error on the FX Application Thread
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load employee data from the database: " + e.getMessage());
                employeeList.clear(); // Clear table on error
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
        // Format salary consistently when displaying
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
        // Basic phone check (allows digits, spaces, +, -, (, ))
        String phone = phoneField.getText();
        if (phone == null || phone.trim().isEmpty()) {
            errorMessage.append("Phone number is required.\n");
        } else if (!phone.matches("^[\\d\\s()+-]*$")) {
            errorMessage.append("Invalid phone number format (only digits, spaces, +, -, () allowed).\n");
        }

        if (hireDatePicker.getValue() == null) {
            errorMessage.append("Hire date is required.\n");
        } else if (hireDatePicker.getValue().isAfter(LocalDate.now())) {
            // Allow hiring today, but not in the future
            errorMessage.append("Hire date cannot be in the future.\n");
        }

        if (departmentField.getText() == null || departmentField.getText().trim().isEmpty()) {
            errorMessage.append("Department is required.\n");
        }
        if (jobTitleField.getText() == null || jobTitleField.getText().trim().isEmpty()) {
            errorMessage.append("Job title is required.\n");
        }
        // Salary validation (must be a positive number)
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
            return false; // Validation failed
        }

        return true; // All validations passed
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // called when the app shuts down, should be called.
    public void shutdown() {
        System.out.println("EmployeeController shutdown: Requesting DB connection closure.");
        employeeService.closeDatabaseConnection();
    }
}