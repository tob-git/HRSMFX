package com.example.hrsm2.controller;

import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.service.EmployeeService; // Assuming EmployeeService uses DB now
import com.example.hrsm2.service.LeaveRequestService;
import com.example.hrsm2.event.EmployeeEvent;
import com.example.hrsm2.event.EventManager;
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
import java.util.List; // Import List
import java.util.ResourceBundle;
import javafx.application.Platform;

public class LeaveController implements Initializable {
    @FXML private TableView<LeaveRequest> leaveRequestTable;
    @FXML private TableColumn<LeaveRequest, Integer> idColumn; // Data type changed to Integer
    @FXML private TableColumn<LeaveRequest, String> employeeIdColumn;
    @FXML private TableColumn<LeaveRequest, LocalDate> startDateColumn;
    @FXML private TableColumn<LeaveRequest, LocalDate> endDateColumn;
    @FXML private TableColumn<LeaveRequest, Long> daysColumn; // Keep as Long for display
    @FXML private TableColumn<LeaveRequest, String> reasonColumn;
    @FXML private TableColumn<LeaveRequest, LeaveRequest.LeaveStatus> statusColumn;
    @FXML private TableColumn<LeaveRequest, String> commentsColumn;

    @FXML private ComboBox<Employee> employeeComboBox;
    @FXML private Label availableDaysLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label requestedDaysLabel;
    @FXML private TextArea reasonArea;
    @FXML private TextArea commentsArea;
    @FXML private Button submitButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button clearButton;
    // Optional: Add Delete button
    // @FXML private Button deleteButton;

    // Use Singleton instances of services
    private final EmployeeService employeeService = EmployeeService.getInstance();
    private final LeaveRequestService leaveRequestService = LeaveRequestService.getInstance();

    private ObservableList<LeaveRequest> leaveRequestList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    private LeaveRequest selectedLeaveRequest;

    // Default available leave days per employee - Service handles the actual calculation
    private static final int DEFAULT_AVAILABLE_LEAVE_DAYS = 20; // Keep for display consistency if needed, but calculation is service-side

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id")); // Use Integer ID field
        employeeIdColumn.setCellValueFactory(cellData -> {
            LeaveRequest request = cellData.getValue();
            // Fetch Employee from DB via EmployeeService (assuming it's updated)
            Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
            return employee != null ?
                    javafx.beans.binding.Bindings.createStringBinding(employee::getFullName) :
                    javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown [" + request.getEmployeeId() + "]");
        });
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        daysColumn.setCellValueFactory(cellData -> {
            LeaveRequest request = cellData.getValue();
            // Use the model's calculation method
            long days = request.getDurationInDays();
            return new javafx.beans.property.SimpleLongProperty(days).asObject();
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("managerComments")); // Field name matches DB/Model

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
        setupEmployeeComboBox();

        // When employee is selected, update available days display
        employeeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                updateAvailableDaysDisplay(newValue);
            } else {
                availableDaysLabel.setText("-"); // Indicate no employee selected
            }
        });

        // Setup table selection listener
        leaveRequestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedLeaveRequest = newSelection; // Update selection
            if (newSelection != null) {
                showLeaveRequestDetails(newSelection);
                boolean isPending = newSelection.getStatus() == LeaveRequest.LeaveStatus.PENDING;
                approveButton.setDisable(!isPending);
                rejectButton.setDisable(!isPending);
                submitButton.setDisable(true); // Disable submit when viewing existing
                // deleteButton.setDisable(false); // Enable delete when selected
            } else {
                clearSelectionDependentFields();
            }
        });

        // Initialize button states
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        // deleteButton.setDisable(true);

        // Load employees from DB via service
        loadEmployees();

        // Load initial leave request data from DB via service
        refreshLeaveRequestList();

        // Initial update of requested days
        updateRequestedDays();
        
        // Register for employee events
        registerForEmployeeEvents();
    }

    private void setupDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (java.time.format.DateTimeParseException e) {
                        return null; // Or handle parse error
                    }
                } else {
                    return null;
                }
            }
        });
        // Optional: Prevent selecting past dates for start date
        // startDatePicker.setDayCellFactory(picker -> new DateCell() {
        //     @Override
        //     public void updateItem(LocalDate date, boolean empty) {
        //         super.updateItem(date, empty);
        //         setDisable(empty || date.isBefore(LocalDate.now()));
        //     }
        // });
    }

    private void setupEmployeeComboBox() {
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee employee) {
                return employee == null ? "" : employee.getFullName() + " (" + employee.getId().substring(0, 8) + "...)"; // Show partial ID for clarity
            }

            @Override
            public Employee fromString(String string) {
                // Find employee by displayed string (more robust lookup might be needed)
                return employeeList.stream()
                        .filter(e -> toString(e).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }


    private void loadEmployees() {
        try {
            List<Employee> employeesFromDb = employeeService.getAllEmployees(); // Fetch from DB
            employeeList.setAll(employeesFromDb); // Update observable list
            employeeComboBox.setItems(employeeList);

            if (!employeeList.isEmpty()) {
                // Select first employee by default or based on some logic
                // employeeComboBox.getSelectionModel().selectFirst();
                // updateAvailableDaysDisplay(employeeList.get(0));
            } else {
                availableDaysLabel.setText("-");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load employees: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update the display label based on service calculation
    private void updateAvailableDaysDisplay(Employee employee) {
        if (employee == null || employee.getId() == null) {
            availableDaysLabel.setText("-");
            return;
        }
        try {
            int usedDays = leaveRequestService.getApprovedLeaveDaysForEmployee(employee.getId());
            int availableDays = DEFAULT_AVAILABLE_LEAVE_DAYS - usedDays; // Use the default allowance for now
            availableDaysLabel.setText(String.valueOf(Math.max(0, availableDays)));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to calculate available days: " + e.getMessage());
            availableDaysLabel.setText("Error");
        }
    }

    private void updateRequestedDays() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                requestedDaysLabel.setText("Invalid");
                requestedDaysLabel.setStyle("-fx-text-fill: red;");
            } else {
                long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                requestedDaysLabel.setText(String.valueOf(days));
                requestedDaysLabel.setStyle(""); // Reset style
            }
        } else {
            requestedDaysLabel.setText("0");
            requestedDaysLabel.setStyle(""); // Reset style
        }
    }

    @FXML
    private void handleSubmitLeaveRequest() {
        // Validate inputs first
        if (!validateInputs(true)) { // Pass true to check available days
            return;
        }

        Employee selectedEmployee = employeeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String reason = reasonArea.getText().trim();

        // Create leave request object (ID will be null)
        LeaveRequest newRequest = new LeaveRequest(
                selectedEmployee.getId(),
                startDate,
                endDate,
                reason
                // Status defaults to PENDING in constructor
        );

        try {
            // Submit request via service (which now uses DB)
            boolean success = leaveRequestService.submitLeaveRequest(newRequest);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully (ID: " + newRequest.getId() + ").");
                clearForm(); // Clear form fields but keep employee selected
                refreshLeaveRequestList(); // Refresh table from DB
                updateAvailableDaysDisplay(selectedEmployee); // Update available days display
            } else {
                // Service layer should ideally provide more specific error feedback
                showAlert(Alert.AlertType.ERROR, "Submission Failed", "Failed to submit leave request. Check logs or service messages. Possible reasons: Overlapping dates, insufficient available days.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit leave request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApproveLeaveRequest() {
        if (selectedLeaveRequest == null || selectedLeaveRequest.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a pending leave request to approve.");
            return;
        }
        if (selectedLeaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Invalid Status", "Only PENDING requests can be approved.");
            return;
        }

        String comments = commentsArea.getText().trim(); // Get comments from UI

        try {
            // Approve request via service (using Integer ID)
            boolean success = leaveRequestService.approveLeaveRequest(selectedLeaveRequest.getId(), comments);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request (ID: " + selectedLeaveRequest.getId() + ") approved.");
                refreshLeaveRequestList();
                clearForm(); // Includes clearing selection
                // Optionally update available days if the approved employee is still selected
                if(employeeComboBox.getValue() != null && employeeComboBox.getValue().getId().equals(selectedLeaveRequest.getEmployeeId())) {
                    updateAvailableDaysDisplay(employeeComboBox.getValue());
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Approval Failed", "Failed to approve leave request (ID: " + selectedLeaveRequest.getId() + ").");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error approving request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRejectLeaveRequest() {
        if (selectedLeaveRequest == null || selectedLeaveRequest.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a pending leave request to reject.");
            return;
        }
        if (selectedLeaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Invalid Status", "Only PENDING requests can be rejected.");
            return;
        }

        String comments = commentsArea.getText().trim();

        // Validate manager comments are provided for rejection
        if (comments.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Manager comments are required when rejecting a leave request.");
            commentsArea.requestFocus(); // Focus the comments area
            return;
        }

        try {
            // Reject request via service (using Integer ID)
            boolean success = leaveRequestService.rejectLeaveRequest(selectedLeaveRequest.getId(), comments);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request (ID: " + selectedLeaveRequest.getId() + ") rejected.");
                refreshLeaveRequestList();
                clearForm(); // Includes clearing selection
                // No need to update available days on rejection
            } else {
                showAlert(Alert.AlertType.ERROR, "Rejection Failed", "Failed to reject leave request (ID: " + selectedLeaveRequest.getId() + ").");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error rejecting request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* Optional: Implement Delete Handler
    @FXML
    private void handleDeleteLeaveRequest() {
        if (selectedLeaveRequest == null || selectedLeaveRequest.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a leave request to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete leave request ID: " + selectedLeaveRequest.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText(null);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean success = leaveRequestService.deleteLeaveRequest(selectedLeaveRequest.getId());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request deleted successfully.");
                        refreshLeaveRequestList();
                        clearForm();
                         // Optionally update available days if the deleted request affected the selected employee
                         if(employeeComboBox.getValue() != null && employeeComboBox.getValue().getId().equals(selectedLeaveRequest.getEmployeeId())) {
                             updateAvailableDaysDisplay(employeeComboBox.getValue());
                         }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Failed to delete leave request.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error deleting request: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    */


    @FXML
    private void handleClearForm() {
        clearForm();
    }

    // Refreshes the table data from the database
    public void refreshLeaveRequestList() {
        try {
            List<LeaveRequest> requestsFromDb = leaveRequestService.getAllLeaveRequests();
            leaveRequestList.setAll(requestsFromDb); // Update the observable list
            leaveRequestTable.setItems(leaveRequestList); // Set items for the table
            // Optional: Re-apply sort order if needed
            // leaveRequestTable.sort();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load leave requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Populates the form fields when a table row is selected
    private void showLeaveRequestDetails(LeaveRequest leaveRequest) {
        if (leaveRequest == null) return;

        // Find and select the employee in the ComboBox
        employeeList.stream()
                .filter(emp -> emp.getId().equals(leaveRequest.getEmployeeId()))
                .findFirst()
                .ifPresent(employeeComboBox::setValue); // Set the value, which triggers listener

        startDatePicker.setValue(leaveRequest.getStartDate());
        endDatePicker.setValue(leaveRequest.getEndDate());
        reasonArea.setText(leaveRequest.getReason());
        commentsArea.setText(leaveRequest.getManagerComments()); // Use correct getter

        updateRequestedDays(); // Update calculated days display
    }

    // Clears form fields and resets button states
    private void clearForm() {
        // Don't clear employee selection - user might want to submit another for same employee
        // employeeComboBox.getSelectionModel().clearSelection(); // Optionally clear employee too
        startDatePicker.setValue(LocalDate.now().plusDays(1)); // Reset dates
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        reasonArea.clear();
        // commentsArea.clear(); // Keep comments visible after approve/reject until explicitly cleared? Or clear here. Let's clear.
        commentsArea.clear();
        leaveRequestTable.getSelectionModel().clearSelection(); // Important to clear table selection
        // selectedLeaveRequest = null; // Done by listener
        clearSelectionDependentFields(); // Reset buttons etc.
        updateRequestedDays();
        // Keep available days display updated for the currently selected employee
        if (employeeComboBox.getValue() != null) {
            updateAvailableDaysDisplay(employeeComboBox.getValue());
        } else {
            availableDaysLabel.setText("-");
        }
    }

    // Resets fields/buttons dependent on table selection
    private void clearSelectionDependentFields() {
        selectedLeaveRequest = null;
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        // deleteButton.setDisable(true);
        submitButton.setDisable(false); // Re-enable submit button when no selection
        commentsArea.clear(); // Clear comments when selection is cleared
    }


    // Validates form inputs before submission or potentially update
    private boolean validateInputs(boolean checkAvailableDays) { // Added flag
        StringBuilder errorMessage = new StringBuilder();
        boolean valid = true; // Assume valid initially

        Employee selectedEmployee = employeeComboBox.getValue();
        if (selectedEmployee == null) {
            errorMessage.append("Employee must be selected.\n");
            valid = false;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null) {
            errorMessage.append("Start date is required.\n");
            valid = false;
        }

        if (endDate == null) {
            errorMessage.append("End date is required.\n");
            valid = false;
        }

        // Date logic checks only if both dates are present
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                errorMessage.append("End date cannot be before start date.\n");
                valid = false;
            }

            // Prevent selecting start dates in the past (optional, can be enforced by DateCellFactory too)
            if (startDate.isBefore(LocalDate.now())) {
                errorMessage.append("Start date cannot be in the past.\n");
                valid = false;
            }

            // Check available days only if requested
            if (checkAvailableDays && selectedEmployee != null) {
                try {
                    long requestedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    if (requestedDays > 0) {
                        int usedDays = leaveRequestService.getApprovedLeaveDaysForEmployee(selectedEmployee.getId());
                        int availableDays = DEFAULT_AVAILABLE_LEAVE_DAYS - usedDays;
                        if (requestedDays > availableDays) {
                            errorMessage.append("Not enough available leave days (Requested: ").append(requestedDays)
                                    .append(", Available: ").append(Math.max(0, availableDays)).append(").\n");
                            valid = false;
                        }
                    }
                } catch (Exception e) {
                    errorMessage.append("Could not verify available leave days: ").append(e.getMessage()).append("\n");
                    valid = false; // Treat error during check as validation failure
                }
            }
        }

        if (reasonArea.getText().trim().isEmpty()) {
            errorMessage.append("Reason is required.\n");
            valid = false;
        }

        if (!valid) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
        }

        return valid;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(content);
        alert.showAndWait();
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
                // If the updated employee is currently selected, update the available days display
                if (employeeComboBox.getValue() != null && 
                    employeeComboBox.getValue().getId().equals(event.getEmployee().getId())) {
                    updateAvailableDaysDisplay(event.getEmployee());
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
                    availableDaysLabel.setText("-");
                }
            });
        });
    }
}