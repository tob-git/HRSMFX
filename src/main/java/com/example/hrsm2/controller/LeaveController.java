package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.LeaveRequestService;
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
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class LeaveController implements Initializable {
    @FXML
    private TableView<LeaveRequest> leaveRequestTable;
    
    @FXML
    private TableColumn<LeaveRequest, String> idColumn;
    
    @FXML
    private TableColumn<LeaveRequest, String> employeeIdColumn;
    
    @FXML
    private TableColumn<LeaveRequest, LocalDate> startDateColumn;
    
    @FXML
    private TableColumn<LeaveRequest, LocalDate> endDateColumn;
    
    @FXML
    private TableColumn<LeaveRequest, Long> daysColumn;
    
    @FXML
    private TableColumn<LeaveRequest, String> reasonColumn;
    
    @FXML
    private TableColumn<LeaveRequest, LeaveRequest.LeaveStatus> statusColumn;
    
    @FXML
    private TableColumn<LeaveRequest, String> commentsColumn;
    
    @FXML
    private ComboBox<Employee> employeeComboBox;
    
    @FXML
    private Label availableDaysLabel;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private Label requestedDaysLabel;
    
    @FXML
    private TextArea reasonArea;
    
    @FXML
    private TextArea commentsArea;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button approveButton;
    
    @FXML
    private Button rejectButton;
    
    @FXML
    private Button clearButton;
    
    private final EmployeeService employeeService = EmployeeService.getInstance();
    private final LeaveRequestService leaveRequestService = LeaveRequestService.getInstance();
    private ObservableList<LeaveRequest> leaveRequestList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private LeaveRequest selectedLeaveRequest;
    
    // Default available leave days per employee
    private static final int DEFAULT_AVAILABLE_LEAVE_DAYS = 20;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeIdColumn.setCellValueFactory(cellData -> {
            LeaveRequest request = cellData.getValue();
            Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
            return employee != null ? 
                javafx.beans.binding.Bindings.createStringBinding(() -> employee.getFullName()) : 
                javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown");
        });
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        daysColumn.setCellValueFactory(cellData -> {
            LeaveRequest request = cellData.getValue();
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            return new javafx.beans.property.SimpleLongProperty(days).asObject();
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("managerComments"));
        
        // Setup date pickers
        setupDatePicker(startDatePicker);
        setupDatePicker(endDatePicker);
        
        // Set default values for date pickers
        startDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Listener for date changes to update requested days
        startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> updateRequestedDays());
        endDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> updateRequestedDays());
        
        // Setup employee combo box
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee employee) {
                return employee == null ? "" : employee.getFullName();
            }
            
            @Override
            public Employee fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        // When employee is selected, update available days
        employeeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                updateAvailableDays(newValue);
            }
        });
        
        // Setup table selection listener
        leaveRequestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedLeaveRequest = newSelection;
                showLeaveRequestDetails(selectedLeaveRequest);
                boolean isPending = selectedLeaveRequest.getStatus() == LeaveRequest.LeaveStatus.PENDING;
                approveButton.setDisable(!isPending);
                rejectButton.setDisable(!isPending);
                submitButton.setDisable(true);
            } else {
                selectedLeaveRequest = null;
                approveButton.setDisable(true);
                rejectButton.setDisable(true);
                submitButton.setDisable(false);
            }
        });
        
        // Initialize button states
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        
        // Load employees
        loadEmployees();
        
        // Load initial leave request data
        refreshLeaveRequestList();
        
        // Initial update of requested days
        updateRequestedDays();
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
        employeeList.addAll(employeeService.getAllEmployees());
        employeeComboBox.setItems(employeeList);
        
        if (!employeeList.isEmpty()) {
            employeeComboBox.getSelectionModel().selectFirst();
            updateAvailableDays(employeeList.get(0));
        }
    }
    
    private void updateAvailableDays(Employee employee) {
        // In a real application, this would fetch from a database
        // Here we'll simulate by showing default days minus taken leave
        int usedDays = leaveRequestService.getApprovedLeaveDaysForEmployee(employee.getId());
        int availableDays = DEFAULT_AVAILABLE_LEAVE_DAYS - usedDays;
        availableDaysLabel.setText(String.valueOf(Math.max(0, availableDays)));
    }
    
    private void updateRequestedDays() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                requestedDaysLabel.setText("Invalid dates");
                requestedDaysLabel.setStyle("-fx-text-fill: red;");
            } else {
                long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                requestedDaysLabel.setText(String.valueOf(days));
                requestedDaysLabel.setStyle("");
            }
        } else {
            requestedDaysLabel.setText("0");
        }
    }
    
    @FXML
    private void handleSubmitLeaveRequest() {
        try {
            // Validate input data
            if (!validateInputs()) {
                return;
            }
            
            Employee selectedEmployee = employeeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String reason = reasonArea.getText();
            
            // Create leave request
            LeaveRequest request = new LeaveRequest(
                selectedEmployee.getId(),
                startDate,
                endDate,
                reason
            );
            
            // Submit request
            boolean success = leaveRequestService.submitLeaveRequest(request);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully.");
                clearForm();
                refreshLeaveRequestList();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit leave request. " +
                       "Ensure the dates don't conflict with existing leave and there are enough available days.");
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit leave request: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleApproveLeaveRequest() {
        if (selectedLeaveRequest == null) {
            return;
        }
        
        try {
            // Set manager comments
            selectedLeaveRequest.setManagerComments(commentsArea.getText());
            
            // Approve request
            boolean success = leaveRequestService.approveLeaveRequest(selectedLeaveRequest.getId());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request approved.");
                refreshLeaveRequestList();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve leave request.");
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error approving request: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRejectLeaveRequest() {
        if (selectedLeaveRequest == null) {
            return;
        }
        
        try {
            // Validate manager comments
            if (commentsArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Manager comments are required when rejecting a request.");
                return;
            }
            
            // Set manager comments
            selectedLeaveRequest.setManagerComments(commentsArea.getText());
            
            // Reject request
            boolean success = leaveRequestService.rejectLeaveRequest(selectedLeaveRequest.getId());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request rejected.");
                refreshLeaveRequestList();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject leave request.");
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error rejecting request: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearForm() {
        clearForm();
    }
    
    private void refreshLeaveRequestList() {
        leaveRequestList.clear();
        leaveRequestList.addAll(leaveRequestService.getAllLeaveRequests());
        leaveRequestTable.setItems(leaveRequestList);
    }
    
    private void showLeaveRequestDetails(LeaveRequest leaveRequest) {
        // Find employee by ID
        for (Employee employee : employeeList) {
            if (employee.getId().equals(leaveRequest.getEmployeeId())) {
                employeeComboBox.setValue(employee);
                break;
            }
        }
        
        startDatePicker.setValue(leaveRequest.getStartDate());
        endDatePicker.setValue(leaveRequest.getEndDate());
        reasonArea.setText(leaveRequest.getReason());
        commentsArea.setText(leaveRequest.getManagerComments());
        
        updateRequestedDays();
    }
    
    private void clearForm() {
        // Don't clear the employee selection
        startDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        reasonArea.clear();
        commentsArea.clear();
        leaveRequestTable.getSelectionModel().clearSelection();
        selectedLeaveRequest = null;
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        submitButton.setDisable(false);
        updateRequestedDays();
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (employeeComboBox.getValue() == null) {
            errorMessage.append("Employee is required.\n");
        }
        
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null) {
            errorMessage.append("Start date is required.\n");
        }
        
        if (endDate == null) {
            errorMessage.append("End date is required.\n");
        }
        
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                errorMessage.append("End date cannot be before start date.\n");
            }
            
            if (startDate.isBefore(LocalDate.now())) {
                errorMessage.append("Start date cannot be in the past.\n");
            }
            
            int availableDays = Integer.parseInt(availableDaysLabel.getText());
            long requestedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            
            if (requestedDays > availableDays) {
                errorMessage.append("Not enough available leave days.\n");
            }
        }
        
        if (reasonArea.getText().trim().isEmpty()) {
            errorMessage.append("Reason is required.\n");
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