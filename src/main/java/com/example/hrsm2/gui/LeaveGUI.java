package com.example.hrsm2.gui;

import com.example.hrsm2.controller.LeaveController;
import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.LeaveRequest;
import com.example.hrsm2.model.User;
import com.example.hrsm2.event.EmployeeEvent;
import com.example.hrsm2.event.EventManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import javafx.application.Platform;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;




public class LeaveGUI extends LeaveController implements Initializable {
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

    @FXML private StackPane notificationPane;

    // Controller for business logic
    private final LeaveController leaveController = new LeaveController();

    private ObservableList<LeaveRequest> leaveRequestList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    private LeaveRequest selectedLeaveRequest;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configure table columns to bind to LeaveRequest properties.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeIdColumn.setCellValueFactory(cellData -> {
            LeaveRequest request = cellData.getValue();
            Employee employee = leaveController.getEmployeeById(request.getEmployeeId());
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
        
        // Register for employee events
        registerForEmployeeEvents();
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
                        return null;
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
                        setDisable(empty || date.isBefore(LocalDate.now()));
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
                return employee == null ? "Select Employee..." : employee.getFullName();
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
            List<Employee> employeesFromDb = leaveController.getAllEmployees();
            employeeList.setAll(employeesFromDb);
            employeeComboBox.setItems(employeeList);

            if (employeeList.isEmpty()) {
                showAlert("No employees found in the database.", NotificationSystem.Type.WARNING, 3);
            }
        } catch (Exception e) {
            showAlert("Failed to load employees: " + e.getMessage(), NotificationSystem.Type.ERROR, 3);
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
            int availableDays = leaveController.getAvailableLeaveDays(employee.getId());
            availableDaysLabel.setText(String.valueOf(availableDays)); // Already ensures non-negative in controller
        } catch (Exception e) {
            showAlert("Failed to calculate available days: " + e.getMessage(), NotificationSystem.Type.ERROR, 3);
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
            requestedDaysLabel.setStyle("");
        }
    }

    @FXML
    public void handleSubmitLeaveRequest() {
        // Clear any previous selection
        leaveRequestTable.getSelectionModel().clearSelection();

        if (!validateInputs(true)) {
            return; // Exit if validation fails.
        }

        if (employeeComboBox.getValue() == null) {
            NotificationSystem.showWarning(notificationPane, "Please select an employee for this leave request.");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String reason = reasonArea.getText().trim();
        String employeeId = employeeComboBox.getValue().getId();

        try {
            // Submit request through the controller
            boolean success = leaveController.submitLeaveRequest(employeeId, startDate, endDate, reason);

            if (success) {
                NotificationSystem.showSuccess(notificationPane, "Leave request submitted successfully.");
                clearForm(); // Reset form for new entry.
                refreshLeaveRequestList(); // Update table to show new request.
            } else {
                NotificationSystem.showError(notificationPane, "Failed to submit leave request.");
            }
        } catch (Exception e) {
            NotificationSystem.showError(notificationPane, "An error occurred during submission: " + e.getMessage());
            e.printStackTrace(); // Log for debugging.
        }
    }

    @FXML
    public void handleApproveLeaveRequest() {
        if (selectedLeaveRequest == null) {
            NotificationSystem.showWarning(notificationPane, "Please select a leave request to approve.");
            return;
        }

        if (selectedLeaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            NotificationSystem.showWarning(notificationPane, "Only PENDING requests can be approved.");
            return;
        }

        // Optional: Check if there are enough available days for the employee
        try {
            String employeeId = selectedLeaveRequest.getEmployeeId();
            int availableDays = leaveController.getAvailableLeaveDays(employeeId);
            int requestDays = (int) selectedLeaveRequest.getDurationInDays();

            if (requestDays > availableDays) {
                // Custom confirmation dialog - keeping this as a dialog for important decisions
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Insufficient Leave Days");
                dialog.setHeaderText("This employee doesn't have enough leave days.");
                dialog.setContentText("Requested: " + requestDays + " days, Available: " + availableDays +
                        " days. Do you want to approve anyway?");
                
                ButtonType approveButton = new ButtonType("Approve", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().addAll(approveButton, cancelButton);
                
                if (dialog.showAndWait().orElse(cancelButton) != approveButton) {
                    return; // User canceled the approval.
                }
            }

            // Add null check for commentsArea
            String comments = commentsArea != null && commentsArea.getText() != null 
                    ? commentsArea.getText().trim() 
                    : "";

            // Approve the request through the controller
            boolean success = leaveController.approveLeaveRequest(selectedLeaveRequest.getId(), comments);

            if (success) {
                NotificationSystem.showSuccess(notificationPane, "Leave request approved successfully.");
                refreshLeaveRequestList();
                clearForm();
                leaveRequestTable.getSelectionModel().clearSelection();
            } else {
                NotificationSystem.showError(notificationPane, "Failed to approve the leave request.");
            }
        } catch (Exception e) {
            NotificationSystem.showError(notificationPane, "An error occurred during approval: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRejectLeaveRequest() {
        if (selectedLeaveRequest == null) {
            NotificationSystem.showWarning(notificationPane, "Please select a leave request to reject.");
            return;
        }

        if (selectedLeaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            NotificationSystem.showWarning(notificationPane, "Only PENDING requests can be rejected.");
            return;
        }

        try {
            // Add null check for commentsArea
            String comments = commentsArea != null && commentsArea.getText() != null 
                    ? commentsArea.getText().trim() 
                    : "";

            if (comments.isEmpty()) {
                // Manager should provide a reason for rejection - keeping this as a dialog for important decisions
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Missing Reason");
                dialog.setHeaderText("No reason for rejection provided.");
                dialog.setContentText("Are you sure you want to reject without providing comments?");
                
                ButtonType yesButton = new ButtonType("Yes, Continue", ButtonBar.ButtonData.OK_DONE);
                ButtonType noButton = new ButtonType("No, Add Reason", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().setAll(yesButton, noButton);
                
                if (dialog.showAndWait().orElse(noButton) == noButton) {
                    return; // User wants to add a rejection reason
                }
            }

            // Reject the request through the controller
            boolean success = leaveController.rejectLeaveRequest(selectedLeaveRequest.getId(), comments);

            if (success) {
                NotificationSystem.showSuccess(notificationPane, "Leave request rejected successfully.");
                refreshLeaveRequestList();
                clearForm();
                leaveRequestTable.getSelectionModel().clearSelection();
            } else {
                NotificationSystem.showError(notificationPane, "Failed to reject the leave request.");
            }
        } catch (Exception e) {
            NotificationSystem.showError(notificationPane, "An error occurred during rejection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClearForm() {
        clearForm();
        leaveRequestTable.getSelectionModel().clearSelection();
    }

    public void refreshLeaveRequestList() {
        try {
            List<LeaveRequest> leaveRequests = leaveController.getAllLeaveRequests();
            leaveRequestList.setAll(leaveRequests);
            leaveRequestTable.setItems(leaveRequestList);
        } catch (Exception e) {
            NotificationSystem.showError(notificationPane, "Failed to load leave requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLeaveRequestDetails(LeaveRequest leaveRequest) {
        // Populate the form with details from the selected request
        Employee employee = leaveController.getEmployeeById(leaveRequest.getEmployeeId());
        if (employee != null) {
            employeeComboBox.setValue(employee);
            updateAvailableDaysDisplay(employee);
        } else {
            employeeComboBox.getSelectionModel().clearSelection();
            availableDaysLabel.setText("-");
        }

        startDatePicker.setValue(leaveRequest.getStartDate());
        endDatePicker.setValue(leaveRequest.getEndDate());
        reasonArea.setText(leaveRequest.getReason());
        commentsArea.setText(leaveRequest.getManagerComments());

        // Update requested days
        updateRequestedDays();
    }

    private void clearForm() {
        employeeComboBox.getSelectionModel().clearSelection();
        startDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        reasonArea.clear();
        commentsArea.clear();
        availableDaysLabel.setText("-");
        // Reset labels and buttons
        updateRequestedDays();
        submitButton.setDisable(false);
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        selectedLeaveRequest = null;
    }

    // Clear fields that depend on a selection, but keep some form data
    private void clearSelectionDependentFields() {
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        submitButton.setDisable(false);
        commentsArea.clear();
        employeeComboBox.setDisable(false);
        startDatePicker.setDisable(false);
        endDatePicker.setDisable(false);
        reasonArea.setDisable(false);
    }
    
    // Validates form inputs, with option to check against available days
    private boolean validateInputs(boolean checkAvailableDays) {
        StringBuilder errorMessage = new StringBuilder();

        // Validate employee selection
        if (employeeComboBox.getValue() == null) {
            NotificationSystem.showError(notificationPane, "Please select an employee.");
            return false;
        }

        // Validate date selection
        if (startDatePicker.getValue() == null) {
            NotificationSystem.showError(notificationPane, "Please select a start date.");
            return false;
        }

        if (endDatePicker.getValue() == null) {
            NotificationSystem.showError(notificationPane, "Please select an end date.");
            return false;
        } else if (startDatePicker.getValue() != null) {
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                NotificationSystem.showError(notificationPane, "End date cannot be before start date.");
                return false;
            }
        }

        // Check if dates are in the past
        LocalDate today = LocalDate.now();
        if (startDatePicker.getValue() != null && startDatePicker.getValue().isBefore(today)) {
            NotificationSystem.showError(notificationPane, "Start date cannot be in the past.");
            return false;
        }

        // Validate reason text
        String reason = reasonArea.getText().trim();
        if (reason.isEmpty()) {
            NotificationSystem.showError(notificationPane, "Please provide a reason for the leave request.");
            return false;
        } else if (reason.length() < 5) {
            NotificationSystem.showError(notificationPane, "Reason should be more descriptive (at least 5 characters).");
            return false;
        }

        // If checking against available days, validate the number of days requested
        if (checkAvailableDays && employeeComboBox.getValue() != null &&
                startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            try {
                // Calculate requested days
                long requestedDays = ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue()) + 1;
                String employeeId = employeeComboBox.getValue().getId();
                int availableDays = leaveController.getAvailableLeaveDays(employeeId);

                if (requestedDays > availableDays) {
                    // Warning only, not a hard error - keeping this as a dialog for important decisions
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Insufficient Leave Days");
                    dialog.setHeaderText("Employee has insufficient leave days");
                    dialog.setContentText("Employee has " + availableDays + " days available " +
                            "but is requesting " + requestedDays + " days.\n\nDo you want to continue anyway?");
                    ButtonType yesButton = new ButtonType("Yes, Continue", ButtonBar.ButtonData.OK_DONE);
                    ButtonType noButton = new ButtonType("No, Adjust Request", ButtonBar.ButtonData.CANCEL_CLOSE);
                    dialog.getDialogPane().getButtonTypes().setAll(yesButton, noButton);

                    if (dialog.showAndWait().orElse(noButton) == noButton) {
                        return false;
                    }
                }
            } catch (Exception e) {
                // Log the error but continue validation
                System.err.println("Error checking available days: " + e.getMessage());
                e.printStackTrace();
            }
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
     * Register for employee events so the employee list refreshes when there are changes
     */
    private void registerForEmployeeEvents() {
        EventManager eventManager = EventManager.getInstance();
        
        // Listen for employee added events
        eventManager.addEventHandler(EmployeeEvent.EMPLOYEE_ADDED, event -> {
            Platform.runLater(() -> {
                loadEmployees(); // Loads from controller
            });
        });
        
        // Listen for employee updated events
        eventManager.addEventHandler(EmployeeEvent.EMPLOYEE_UPDATED, event -> {
            Platform.runLater(() -> {
                loadEmployees(); // Loads from controller
                if (employeeComboBox.getValue() != null && 
                    employeeComboBox.getValue().getId().equals(event.getEmployee().getId())) {
                    // Update the selection to reflect changes
                    updateAvailableDaysDisplay(event.getEmployee());
                }
            });
        });
        
        // Listen for employee deleted events
        eventManager.addEventHandler(EmployeeEvent.EMPLOYEE_DELETED, event -> {
            Platform.runLater(() -> {
                loadEmployees(); // Loads from controller
                // If the deleted employee was selected, clear the selection
                if (employeeComboBox.getValue() != null && 
                    employeeComboBox.getValue().getId().equals(event.getEmployee().getId())) {
                    employeeComboBox.getSelectionModel().clearSelection();
                    availableDaysLabel.setText("-");
                }
            });
        });
    }
    
    public void shutdown() {
        // Cleanup when the application shuts down
    }
}