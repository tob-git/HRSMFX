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
import java.util.List;
import java.util.ResourceBundle;

public class LeaveController implements Initializable {
    @FXML private TableView<LeaveRequest> leaveRequestTable;
    @FXML private TableColumn<LeaveRequest, Integer> idColumn;
    @FXML private TableColumn<LeaveRequest, String> employeeIdColumn;
    @FXML private TableColumn<LeaveRequest, LocalDate> startDateColumn;
    @FXML private TableColumn<LeaveRequest, LocalDate> endDateColumn;
    @FXML private TableColumn<LeaveRequest, Long> daysColumn;
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

    // Use Singleton instances to ensure a single point of access to services.
    private final EmployeeService employeeService = EmployeeService.getInstance();
    private final LeaveRequestService leaveRequestService = LeaveRequestService.getInstance();

    private ObservableList<LeaveRequest> leaveRequestList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    private LeaveRequest selectedLeaveRequest;

    // Represents the default total leave days allowance per employee. Actual available days are calculated by the service.
    private static final int DEFAULT_AVAILABLE_LEAVE_DAYS = 20;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configure table columns to bind to LeaveRequest properties.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeIdColumn.setCellValueFactory(cellData -> {
            LeaveRequest request = cellData.getValue();
            Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
            // Display employee's full name; use binding for potential reactivity.
            return employee != null ?
                    javafx.beans.binding.Bindings.createStringBinding(employee::getFullName) :
                    javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown [" + request.getEmployeeId() + "]");
        });
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        daysColumn.setCellValueFactory(cellData -> {
            // Display the calculated duration from the model.
            long days = cellData.getValue().getDurationInDays();
            return new javafx.beans.property.SimpleLongProperty(days).asObject();
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("managerComments"));

        setupDatePicker(startDatePicker);
        setupDatePicker(endDatePicker);

        startDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(LocalDate.now().plusDays(1));

        startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> updateRequestedDays());
        endDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> updateRequestedDays());

        setupEmployeeComboBox();

        // Update available days when an employee is selected in the ComboBox.
        employeeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                updateAvailableDaysDisplay(newValue);
            } else {
                availableDaysLabel.setText("-");
            }
        });

        // Update form and button states when a table row selection changes.
        leaveRequestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedLeaveRequest = newSelection;
            if (newSelection != null) {
                showLeaveRequestDetails(newSelection);
                boolean isPending = newSelection.getStatus() == LeaveRequest.LeaveStatus.PENDING;
                approveButton.setDisable(!isPending);
                rejectButton.setDisable(!isPending);
                submitButton.setDisable(true); // Cannot submit when viewing an existing request.
            } else {
                clearSelectionDependentFields();
            }
        });

        approveButton.setDisable(true);
        rejectButton.setDisable(true);

        loadEmployees();
        refreshLeaveRequestList();
        updateRequestedDays();
    }

    // Configures DatePicker formatting and restricts selectable dates.
    private void setupDatePicker(DatePicker datePicker) {
        // Custom string converter for consistent date format (yyyy-MM-dd).
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
                        System.err.println("Invalid date format entered: " + string);
                        return null; // Invalid format returns null.
                    }
                } else {
                    return null;
                }
            }
        });

        // Disable past dates for the start date picker.
        if (datePicker == startDatePicker) {
            startDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });
        }

        // Ensure end date is not before the selected start date.
        if (datePicker == endDatePicker) {
            endDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate startDate = startDatePicker.getValue();
                    if (startDate != null) {
                        setDisable(empty || date.isBefore(startDate));
                    } else {
                        setDisable(empty || date.isBefore(LocalDate.now())); // Default if start date isn't picked.
                    }
                }
            });
        }
    }

    // Configures how Employee objects are displayed in the ComboBox.
    private void setupEmployeeComboBox() {
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee employee) {
                // Shows full name and a partial ID for clarity.
                return employee == null ? "Select Employee..." : employee.getFullName() + " (" + employee.getId().substring(0, Math.min(8, employee.getId().length())) + "...)";
            }

            @Override
            public Employee fromString(String string) {
                // Required for editable ComboBox, but not strictly necessary if non-editable.
                // Finds employee based on the formatted string.
                return employeeList.stream()
                        .filter(e -> toString(e).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    // Loads employee data from the service into the ComboBox.
    private void loadEmployees() {
        try {
            List<Employee> employeesFromDb = employeeService.getAllEmployees();
            employeeList.setAll(employeesFromDb);
            employeeComboBox.setItems(employeeList);

            if (employeeList.isEmpty()) {
                availableDaysLabel.setText("-");
                showAlert(Alert.AlertType.WARNING, "No Employees", "No employees found in the database.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load employees: " + e.getMessage());
            e.printStackTrace(); // Log for debugging.
        }
    }

    // Fetches used leave days from the service and updates the available days label.
    private void updateAvailableDaysDisplay(Employee employee) {
        if (employee == null || employee.getId() == null) {
            availableDaysLabel.setText("-");
            return;
        }
        try {
            int usedDays = leaveRequestService.getApprovedLeaveDaysForEmployee(employee.getId());
            int availableDays = DEFAULT_AVAILABLE_LEAVE_DAYS - usedDays;
            availableDaysLabel.setText(String.valueOf(Math.max(0, availableDays))); // Ensure non-negative display.
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to calculate available days: " + e.getMessage());
            availableDaysLabel.setText("Error");
            e.printStackTrace(); // Log for debugging.
        }
    }

    // Calculates and displays the number of days requested based on selected dates.
    private void updateRequestedDays() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                requestedDaysLabel.setText("Invalid");
                requestedDaysLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); // Highlight invalid range.
            } else {
                long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // Inclusive date range.
                requestedDaysLabel.setText(String.valueOf(days));
                requestedDaysLabel.setStyle(""); // Reset style.
            }
        } else {
            requestedDaysLabel.setText("0");
            requestedDaysLabel.setStyle(""); // Reset style.
        }
    }

    @FXML
    private void handleSubmitLeaveRequest() {
        if (!validateInputs(true)) { // Validate including available days check.
            return;
        }

        Employee selectedEmployee = employeeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String reason = reasonArea.getText().trim();

        LeaveRequest newRequest = new LeaveRequest(
                selectedEmployee.getId(),
                startDate,
                endDate,
                reason
                // Status defaults to PENDING in model constructor.
        );

        try {
            boolean success = leaveRequestService.submitLeaveRequest(newRequest);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully (ID: " + newRequest.getId() + ").");
                // Reset form but keep employee selected for potentially more requests.
                startDatePicker.setValue(LocalDate.now().plusDays(1));
                endDatePicker.setValue(LocalDate.now().plusDays(1));
                reasonArea.clear();
                commentsArea.clear();
                updateRequestedDays();
                refreshLeaveRequestList();
                updateAvailableDaysDisplay(selectedEmployee); // Update display after successful submission.
            } else {
                showAlert(Alert.AlertType.ERROR, "Submission Failed", "Failed to submit leave request. Possible reasons: Overlapping dates, insufficient available days, or database issue (check logs).");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit leave request: " + e.getMessage());
            e.printStackTrace(); // Log for debugging.
        }
    }

    @FXML
    private void handleApproveLeaveRequest() {
        if (selectedLeaveRequest == null || selectedLeaveRequest.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a valid leave request to approve.");
            return;
        }
        if (selectedLeaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Invalid Status", "Only PENDING requests can be approved.");
            return;
        }

        // Capture necessary data before potential UI state changes.
        int requestId = selectedLeaveRequest.getId();
        String employeeIdToUpdate = selectedLeaveRequest.getEmployeeId();
        if (employeeIdToUpdate == null) {
            showAlert(Alert.AlertType.ERROR, "Internal Error", "Selected leave request is missing an Employee ID.");
            return;
        }
        String comments = commentsArea.getText().trim();

        try {
            boolean success = leaveRequestService.approveLeaveRequest(requestId, comments);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request (ID: " + requestId + ") approved.");
                refreshLeaveRequestList(); // Refresh data first.
                clearForm(); // Clear form and selection.

                // Update available days if the affected employee is currently selected in the combo box.
                Employee currentEmployeeInComboBox = employeeComboBox.getValue();
                if(currentEmployeeInComboBox != null && currentEmployeeInComboBox.getId().equals(employeeIdToUpdate)) {
                    updateAvailableDaysDisplay(currentEmployeeInComboBox);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Approval Failed", "Failed to approve leave request (ID: " + requestId + "). The request might have been modified, deleted, or a database issue occurred (check logs).");
            }
        } catch (Exception e) {
            // Catch potential exceptions from service layer or database operations.
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred while approving request ID " + requestId + ": " + e.getMessage());
            e.printStackTrace(); // Log for debugging.
        }
    }

    @FXML
    private void handleRejectLeaveRequest() {
        if (selectedLeaveRequest == null || selectedLeaveRequest.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a valid leave request to reject.");
            return;
        }
        if (selectedLeaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Invalid Status", "Only PENDING requests can be rejected.");
            return;
        }

        String comments = commentsArea.getText().trim();
        // Rejection requires manager comments.
        if (comments.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Manager comments are required when rejecting a leave request.");
            commentsArea.requestFocus();
            return;
        }

        int requestId = selectedLeaveRequest.getId();

        try {
            boolean success = leaveRequestService.rejectLeaveRequest(requestId, comments);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request (ID: " + requestId + ") rejected.");
                refreshLeaveRequestList();
                clearForm();
                // No need to update available days on rejection.
            } else {
                showAlert(Alert.AlertType.ERROR, "Rejection Failed", "Failed to reject leave request (ID: " + requestId + "). The request might have been modified, deleted, or a database issue occurred (check logs).");
            }
        } catch (Exception e) {
            // Catch potential exceptions from service layer or database operations.
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred while rejecting request ID " + requestId + ": " + e.getMessage());
            e.printStackTrace(); // Log for debugging.
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    // Reloads the leave request data from the service into the table.
    private void refreshLeaveRequestList() {
        try {
            List<LeaveRequest> requestsFromDb = leaveRequestService.getAllLeaveRequests();
            leaveRequestList.setAll(requestsFromDb);
            leaveRequestTable.setItems(leaveRequestList);
            leaveRequestTable.getSelectionModel().clearSelection(); // Ensure consistent state after refresh.
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to refresh leave requests: " + e.getMessage());
            e.printStackTrace(); // Log for debugging.
        }
    }

    // Populates the form fields with data from the selected leave request.
    private void showLeaveRequestDetails(LeaveRequest leaveRequest) {
        if (leaveRequest == null) return;

        // Find and select the corresponding employee in the ComboBox.
        employeeList.stream()
                .filter(emp -> emp.getId().equals(leaveRequest.getEmployeeId()))
                .findFirst()
                .ifPresent(employeeComboBox::setValue); // Triggers available days update via listener.

        startDatePicker.setValue(leaveRequest.getStartDate());
        endDatePicker.setValue(leaveRequest.getEndDate());
        reasonArea.setText(leaveRequest.getReason() != null ? leaveRequest.getReason() : "");
        commentsArea.setText(leaveRequest.getManagerComments() != null ? leaveRequest.getManagerComments() : "");

        updateRequestedDays(); // Update calculated days display.
    }

    // Resets the form to its default state for creating a new request.
    private void clearForm() {
        leaveRequestTable.getSelectionModel().clearSelection(); // This triggers the selection listener.
        // Keeping employee selection allows users to quickly submit multiple requests for the same employee.

        startDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        reasonArea.clear();
        commentsArea.clear();

        updateRequestedDays();

        // Ensure available days display is correct for the potentially still-selected employee.
        Employee currentEmployee = employeeComboBox.getValue();
        if (currentEmployee != null) {
            updateAvailableDaysDisplay(currentEmployee);
        } else {
            availableDaysLabel.setText("-");
        }
        submitButton.setDisable(false); // Re-enable submit after clearing.
    }

    // Resets UI elements that depend on whether a table row is selected.
    private void clearSelectionDependentFields() {
        selectedLeaveRequest = null;
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        submitButton.setDisable(false); // Enable submit when no request is selected.
        commentsArea.clear();
        // Allow editing fields for a new request.
        commentsArea.setEditable(true);
        reasonArea.setEditable(true);
        startDatePicker.setEditable(true);
        endDatePicker.setEditable(true);
        employeeComboBox.setDisable(false);
    }

    // Performs validation checks on the form inputs.
    private boolean validateInputs(boolean checkAvailableDays) {
        StringBuilder errorMessage = new StringBuilder();
        boolean valid = true;

        Employee selectedEmployee = employeeComboBox.getValue();
        if (selectedEmployee == null) {
            errorMessage.append(" - Employee must be selected.\n");
            valid = false;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null) {
            errorMessage.append(" - Start date is required.\n");
            valid = false;
        }
        if (endDate == null) {
            errorMessage.append(" - End date is required.\n");
            valid = false;
        }

        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                errorMessage.append(" - End date cannot be before start date.\n");
                valid = false;
            }
            if (startDate.isBefore(LocalDate.now())) {
                // Although DateCellFactory restricts this, add validation layer.
                errorMessage.append(" - Start date cannot be in the past.\n");
                valid = false;
            }

            // Conditionally check if requested days exceed available days.
            if (checkAvailableDays && selectedEmployee != null && valid) {
                try {
                    long requestedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    if (requestedDays > 0) {
                        int usedDays = leaveRequestService.getApprovedLeaveDaysForEmployee(selectedEmployee.getId());
                        int availableDays = DEFAULT_AVAILABLE_LEAVE_DAYS - usedDays;
                        if (requestedDays > availableDays) {
                            errorMessage.append(" - Not enough available leave days (Requested: ").append(requestedDays)
                                    .append(", Available: ").append(Math.max(0, availableDays)).append(").\n");
                            valid = false;
                        }
                    } else {
                        errorMessage.append(" - Invalid date range (0 or negative days).\n");
                        valid = false;
                    }
                } catch (Exception e) {
                    errorMessage.append(" - Could not verify available leave days: ").append(e.getMessage()).append("\n");
                    valid = false; // Fail validation if availability check fails.
                    e.printStackTrace(); // Log error.
                }
            }
        }

        if (reasonArea.getText().trim().isEmpty()) {
            errorMessage.append(" - Reason is required.\n");
            valid = false;
        }

        if (!valid) {
            String finalMessage = "Please correct the following errors:\n" + errorMessage.toString();
            showAlert(Alert.AlertType.ERROR, "Validation Error", finalMessage);
        }

        return valid;
    }

    // Utility method to display alerts to the user.
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Use a TextArea for long messages to ensure readability.
        if (content.length() > 100) {
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
        } else {
            alert.setContentText(content);
            alert.setResizable(false);
        }
        alert.showAndWait();
    }
}