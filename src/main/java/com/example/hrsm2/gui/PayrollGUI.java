package com.example.hrsm2.gui;

import com.example.hrsm2.controller.PayrollController;
import com.example.hrsm2.event.EmployeeEvent;
import com.example.hrsm2.event.EventManager;
import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.Payroll;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.StackPane;
import com.example.hrsm2.gui.NotificationSystem;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * GUI class for Payroll management.
 * This class handles all UI interactions and delegates business logic to PayrollController.
 */
public class PayrollGUI implements Initializable {
    @FXML
    private TableView<Payroll> payrollTable;
    @FXML
    private TableColumn<Payroll, String> idColumn;
    @FXML
    private TableColumn<Payroll, String> employeeIdColumn;
    @FXML
    private TableColumn<Payroll, LocalDate> startDateColumn;
    @FXML
    private TableColumn<Payroll, LocalDate> endDateColumn;
    @FXML
    private TableColumn<Payroll, Double> baseSalaryColumn;
    @FXML
    private TableColumn<Payroll, Double> overtimeColumn;
    @FXML
    private TableColumn<Payroll, Double> bonusColumn;
    @FXML
    private TableColumn<Payroll, Double> taxDeductionsColumn;
    @FXML
    private TableColumn<Payroll, Double> otherDeductionsColumn;
    @FXML
    private TableColumn<Payroll, Double> netSalaryColumn;
    @FXML
    private TableColumn<Payroll, Payroll.PayrollStatus> statusColumn;
    
    @FXML
    private ComboBox<Employee> employeeComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField baseSalaryField;
    @FXML
    private TextField overtimeField;
    @FXML
    private TextField bonusField;
    @FXML
    private TextField taxDeductionsField;
    @FXML
    private TextField otherDeductionsField;
    @FXML
    private TextField netSalaryField;
    
    @FXML
    private Button generateButton;
    @FXML
    private Button generateAllButton;
    @FXML
    private Button processButton;
    @FXML
    private Button markAsPaidButton;
    @FXML
    private Button clearButton;
    
    @FXML
    private StackPane notificationPane;
    
    // Controller for business logic
    private final PayrollController payrollController = new PayrollController();
    
    private ObservableList<Payroll> payrollList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private Payroll selectedPayroll;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeIdColumn.setCellValueFactory(cellData -> {
            Payroll payroll = cellData.getValue();
            // Find the employee by ID
            Employee employee = payrollController.getEmployeeById(payroll.getEmployeeId());
            // Return the full name if found, otherwise return the ID
            return new SimpleStringProperty(employee != null ? 
                employee.getFirstName() + " " + employee.getLastName() : 
                payroll.getEmployeeId());
        });
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("payPeriodStart"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("payPeriodEnd"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        overtimeColumn.setCellValueFactory(new PropertyValueFactory<>("overtimePay"));
        bonusColumn.setCellValueFactory(new PropertyValueFactory<>("bonus"));
        taxDeductionsColumn.setCellValueFactory(new PropertyValueFactory<>("taxDeductions"));
        otherDeductionsColumn.setCellValueFactory(new PropertyValueFactory<>("otherDeductions"));
        netSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("netSalary"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Setup date pickers
        setupDatePicker(startDatePicker);
        setupDatePicker(endDatePicker);
        
        // Setup employee combo box
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee employee) {
                return employee == null ? "Select Employee..." : employee.getFullName();
            }
            
            @Override
            public Employee fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        // Listen for employee selection to update base salary
        employeeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateBaseSalary(newVal);
            }
        });
        
        // Setup fields to update net salary calculation
        baseSalaryField.textProperty().addListener((obs, oldVal, newVal) -> calculateNetSalary());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateNetSalary());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateNetSalary());
        overtimeField.textProperty().addListener((obs, oldVal, newVal) -> calculateNetSalary());
        bonusField.textProperty().addListener((obs, oldVal, newVal) -> calculateNetSalary());
        taxDeductionsField.textProperty().addListener((obs, oldVal, newVal) -> calculateNetSalary());
        otherDeductionsField.textProperty().addListener((obs, oldVal, newVal) -> calculateNetSalary());
        
        // Setup table selection listener
        payrollTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPayroll = newSelection;
                showPayrollDetails(selectedPayroll);
                
                // Enable/disable buttons based on status
                boolean isPending = selectedPayroll.getStatus() == Payroll.PayrollStatus.PENDING;
                boolean isProcessed = selectedPayroll.getStatus() == Payroll.PayrollStatus.PROCESSED;
                
                processButton.setDisable(!isPending);
                markAsPaidButton.setDisable(!isProcessed);
            } else {
                processButton.setDisable(true);
                markAsPaidButton.setDisable(true);
            }
        });
        
        // Initialize buttons state
        processButton.setDisable(true);
        markAsPaidButton.setDisable(true);
        
        // Set initial dates
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate lastOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        startDatePicker.setValue(firstOfMonth);
        endDatePicker.setValue(lastOfMonth);
        
        // Load employees from the service
        loadEmployees();
        
        // Load payroll data
        refreshPayrollList();
        
        // Register for employee events
        registerForEmployeeEvents();
        
        // Initialize fields with zeros
        clearForm();
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
    
    private void loadEmployees() {
        employeeList.clear();
        employeeList.addAll(payrollController.getAllEmployees());
        employeeComboBox.setItems(employeeList);
    }
    
    private void updateBaseSalary(Employee employee) {
        if (employee != null) {
            // Monthly salary calculation
            double monthlySalary = payrollController.calculateMonthlySalary(employee);
            baseSalaryField.setText(String.format("%.2f", monthlySalary));
        } else {
            baseSalaryField.setText("0.00");
        }
    }
    
    private void calculateNetSalary() {
        try {
            double baseSalary = getDoubleFromField(baseSalaryField, 0.0);
            double overtime = getDoubleFromField(overtimeField, 0.0);
            double bonus = getDoubleFromField(bonusField, 0.0);
            double taxDeductions = getDoubleFromField(taxDeductionsField, 0.0);
            double otherDeductions = getDoubleFromField(otherDeductionsField, 0.0);
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            double netSalary = payrollController.calculateNetSalary(
                baseSalary, overtime, bonus, taxDeductions, otherDeductions, startDate, endDate);
            
            netSalaryField.setText(String.format("%.2f", netSalary));
        } catch (NumberFormatException e) {
            netSalaryField.setText("Error");
        }
    }
    
    private double getDoubleFromField(TextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @FXML
    public void handleGeneratePayroll() {
        try {
            // Validate input data
            if (!validateInputs()) {
                return;
            }
            
            Employee selectedEmployee = employeeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            // Generate the payroll entry
            Payroll payroll = payrollController.generatePayroll(selectedEmployee.getId(), startDate, endDate);
            
            if (payroll == null) {
                showAlert("Failed to generate payroll. Employee not found.", NotificationSystem.Type.ERROR, 3);
                return;
            }
            
            // Update with user input values
            payroll.setOvertimePay(getDoubleFromField(overtimeField, 0.0));
            payroll.setBonus(getDoubleFromField(bonusField, 0.0));
            payroll.setTaxDeductions(getDoubleFromField(taxDeductionsField, 0.0));
            payroll.setOtherDeductions(getDoubleFromField(otherDeductionsField, 0.0));
            payroll.calculateNetSalary();
            
            // Update in service
            payrollController.updatePayroll(payroll);
            
            // Show success message
            showAlert("Payroll generated successfully.", NotificationSystem.Type.SUCCESS, 3);
            
            // Refresh list
            refreshPayrollList();
            
            // Clear form
            clearForm();
            
        } catch (Exception e) {
            showAlert("Failed to generate payroll: " + e.getMessage(), NotificationSystem.Type.ERROR, 3);
        }
    }
    
    @FXML
    public void handleGenerateAllPayrolls() {
        try {
            // Validate date inputs
            if (!validateDateInputs()) {
                return;
            }
            
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            // Check if there are employees in the system
            if (employeeList.isEmpty()) {
                showAlert("No employees found. Please add employees first.", NotificationSystem.Type.ERROR, 3);
                return;
            }
            
            // Generate payrolls for all employees
            payrollController.generatePayrollsForAllEmployees(startDate, endDate);
            
            // Show success message
            showAlert("Payrolls generated for all employees.", NotificationSystem.Type.SUCCESS, 3);
            
            // Refresh list
            refreshPayrollList();
            
        } catch (Exception e) {
            showAlert("Failed to generate payrolls: " + e.getMessage(), NotificationSystem.Type.ERROR, 3);
        }
    }
    
    @FXML
    public void handleProcessPayroll() {
        if (selectedPayroll == null) {
            return;
        }
        
        try {
            boolean success = payrollController.processPayroll(selectedPayroll.getId());
            
            if (success) {
                // Show success message
                showAlert("Payroll processed successfully.", NotificationSystem.Type.SUCCESS, 3);
                
                // Refresh list
                refreshPayrollList();
                
                // Clear selection
                payrollTable.getSelectionModel().clearSelection();
                clearForm();
            } else {
                showAlert("Failed to process payroll. Invalid status.", NotificationSystem.Type.ERROR, 3);
            }
        } catch (Exception e) {
            showAlert("Failed to process payroll: " + e.getMessage(), NotificationSystem.Type.ERROR, 3);
        }
    }
    
    @FXML
    public void handleMarkAsPaid() {
        if (selectedPayroll == null) {
            return;
        }
        
        try {
            boolean success = payrollController.markPayrollAsPaid(selectedPayroll.getId());
            
            if (success) {
                // Show success message
                showAlert("Payroll marked as paid successfully.", NotificationSystem.Type.SUCCESS, 3);
                
                // Refresh list
                refreshPayrollList();
                
                // Clear selection
                payrollTable.getSelectionModel().clearSelection();
                clearForm();
            } else {
                showAlert("Failed to mark payroll as paid. Invalid status.", NotificationSystem.Type.ERROR, 3);
            }
        } catch (Exception e) {
            showAlert("Failed to mark payroll as paid: " + e.getMessage(), NotificationSystem.Type.ERROR, 3);
        }
    }
    
    @FXML
    public void handleClearForm() {
        clearForm();
        payrollTable.getSelectionModel().clearSelection();
        selectedPayroll = null;
        processButton.setDisable(true);
        markAsPaidButton.setDisable(true);
    }
    
    public void refreshPayrollList() {
        payrollList.clear();
        payrollList.addAll(payrollController.getAllPayrolls());
        payrollTable.setItems(payrollList);
    }
    
    private void showPayrollDetails(Payroll payroll) {
        // Find employee by ID
        for (Employee employee : employeeList) {
            if (employee.getId().equals(payroll.getEmployeeId())) {
                employeeComboBox.setValue(employee);
                break;
            }
        }
        
        startDatePicker.setValue(payroll.getPayPeriodStart());
        endDatePicker.setValue(payroll.getPayPeriodEnd());
        baseSalaryField.setText(String.format("%.2f", payroll.getBaseSalary()));
        overtimeField.setText(String.format("%.2f", payroll.getOvertimePay()));
        bonusField.setText(String.format("%.2f", payroll.getBonus()));
        taxDeductionsField.setText(String.format("%.2f", payroll.getTaxDeductions()));
        otherDeductionsField.setText(String.format("%.2f", payroll.getOtherDeductions()));
        netSalaryField.setText(String.format("%.2f", payroll.getNetSalary()));
    }
    
    private void clearForm() {
        employeeComboBox.getSelectionModel().clearSelection();
        // Keep the date fields as they are
        
        overtimeField.setText("0.00");
        bonusField.setText("0.00");
        taxDeductionsField.setText("0.00");
        otherDeductionsField.setText("0.00");
        baseSalaryField.setText("0.00");
        netSalaryField.setText("0.00");
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (employeeComboBox.getValue() == null) {
            errorMessage.append("Employee is required.\n");
        }
        
        if (!validateDateInputs()) {
            return false;
        }
        
        try {
            double baseSalary = Double.parseDouble(baseSalaryField.getText());
            if (baseSalary < 0) {
                errorMessage.append("Base salary cannot be negative.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Base salary must be a valid number.\n");
        }
        
        try {
            double overtime = Double.parseDouble(overtimeField.getText());
            if (overtime < 0) {
                errorMessage.append("Overtime pay cannot be negative.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Overtime pay must be a valid number.\n");
        }
        
        try {
            double bonus = Double.parseDouble(bonusField.getText());
            if (bonus < 0) {
                errorMessage.append("Bonus cannot be negative.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Bonus must be a valid number.\n");
        }
        
        try {
            double taxDeductions = Double.parseDouble(taxDeductionsField.getText());
            if (taxDeductions < 0) {
                errorMessage.append("Tax deductions cannot be negative.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Tax deductions must be a valid number.\n");
        }
        
        try {
            double otherDeductions = Double.parseDouble(otherDeductionsField.getText());
            if (otherDeductions < 0) {
                errorMessage.append("Other deductions cannot be negative.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Other deductions must be a valid number.\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert(errorMessage.toString(), NotificationSystem.Type.ERROR, 3);
            return false;
        }
        
        return true;
    }
    
    private boolean validateDateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (startDatePicker.getValue() == null) {
            errorMessage.append("Start date is required.\n");
        }
        
        if (endDatePicker.getValue() == null) {
            errorMessage.append("End date is required.\n");
        } else if (startDatePicker.getValue() != null && endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errorMessage.append("End date cannot be before start date.\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert(errorMessage.toString(), NotificationSystem.Type.ERROR, 3);
            return false;
        }
        
        return true;
    }
    
    /**
     * Shows an alert using the NotificationSystem instead of a traditional alert dialog
     * @param message the message to show
     * @param type the notification type (INFO, SUCCESS, ERROR, WARNING)
     * @param durationInSeconds how long to show the notification
     */
    protected void showAlert(String message, NotificationSystem.Type type, int durationInSeconds) {
        if (notificationPane != null) {
            NotificationSystem.showNotification(notificationPane, message, type, durationInSeconds);
        } else {
            // Fallback to console if the notification pane is not available
            System.out.println(type + ": " + message);
        }
    }
    
    /**
     * Register this controller to listen for employee events.
     * This allows the controller to refresh its employee list when employees are added, updated, or deleted.
     */
    private void registerForEmployeeEvents() {
        EventManager eventManager = EventManager.getInstance();
        
        // Listen for employee added events
        eventManager.addEventHandler(EmployeeEvent.EMPLOYEE_ADDED, event -> {
            Platform.runLater(() -> {
                loadEmployees();
            });
        });
        
        // Listen for employee updated events
        eventManager.addEventHandler(EmployeeEvent.EMPLOYEE_UPDATED, event -> {
            Platform.runLater(() -> {
                loadEmployees();
                // If the updated employee is currently selected, update the base salary
                if (employeeComboBox.getValue() != null && 
                    employeeComboBox.getValue().getId().equals(event.getEmployee().getId())) {
                    updateBaseSalary(event.getEmployee());
                    calculateNetSalary();
                }
            });
        });
        
        // Listen for employee deleted events
        eventManager.addEventHandler(EmployeeEvent.EMPLOYEE_DELETED, event -> {
            Platform.runLater(() -> {
                loadEmployees();
                // If the deleted employee was selected, clear the selection
                if (employeeComboBox.getValue() != null && 
                    employeeComboBox.getValue().getId().equals(event.getEmployee().getId())) {
                    employeeComboBox.getSelectionModel().clearSelection();
                    baseSalaryField.setText("0.00");
                    calculateNetSalary();
                }
            });
        });
    }
    
    // Called when the app shuts down
    public void shutdown() {
        payrollController.shutdown();
    }
} 