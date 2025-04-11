package com.example.hrsm2.gui;

import com.example.hrsm2.controller.PerformanceController;
import com.example.hrsm2.model.Employee;
import com.example.hrsm2.model.PerformanceEvaluation;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.PerformanceEvaluationService;
import com.example.hrsm2.service.UserService;
import com.example.hrsm2.event.EmployeeEvent;
import com.example.hrsm2.event.EventManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;

public class PerformanceGUI implements Initializable {
    @FXML
    private TableView<PerformanceEvaluation> evaluationTable;
    @FXML
    private TableColumn<PerformanceEvaluation, String> idColumn;
    @FXML
    private TableColumn<PerformanceEvaluation, String> employeeIdColumn;
    @FXML
    private TableColumn<PerformanceEvaluation, LocalDate> evaluationDateColumn;
    @FXML
    private TableColumn<PerformanceEvaluation, Integer> ratingColumn;
    @FXML
    private TableColumn<PerformanceEvaluation, String> strengthsColumn;
    @FXML
    private TableColumn<PerformanceEvaluation, String> improvementColumn;
    @FXML
    private TableColumn<PerformanceEvaluation, String> commentsColumn;
    @FXML
    private TableColumn<PerformanceEvaluation, String> reviewedByColumn;
    
    @FXML
    private ComboBox<Employee> employeeComboBox;
    @FXML
    private DatePicker evaluationDatePicker;
    @FXML
    private Slider ratingSlider;
    @FXML
    private Label ratingLabel;
    @FXML
    private TextArea strengthsArea;
    @FXML
    private TextArea improvementArea;
    @FXML
    private TextArea commentsArea;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;
    
    // Controller for business logic
    private final PerformanceController performanceController = new PerformanceController();
    
    private ObservableList<PerformanceEvaluation> evaluationList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private PerformanceEvaluation selectedEvaluation;
    private User currentUser;
    
    // Add StackPane for notifications
    @FXML private StackPane notificationPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get current user from controller
        currentUser = performanceController.getCurrentUser();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeIdColumn.setCellValueFactory(cellData -> {
            PerformanceEvaluation evaluation = cellData.getValue();
            // Find the employee by ID
            Employee employee = performanceController.getEmployeeById(evaluation.getEmployeeId());
            // Return the full name if found, otherwise return the ID
            return new SimpleStringProperty(employee != null ? 
                employee.getFirstName() + " " + employee.getLastName() : 
                evaluation.getEmployeeId());
        });
        evaluationDateColumn.setCellValueFactory(new PropertyValueFactory<>("evaluationDate"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("performanceRating"));
        strengthsColumn.setCellValueFactory(new PropertyValueFactory<>("strengths"));
        improvementColumn.setCellValueFactory(new PropertyValueFactory<>("areasForImprovement"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
        reviewedByColumn.setCellValueFactory(new PropertyValueFactory<>("reviewedBy"));
        
        // Setup date picker
        setupDatePicker(evaluationDatePicker);
        
        // Set today's date as default
        evaluationDatePicker.setValue(LocalDate.now());
        
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
        
        // Setup rating slider
        ratingSlider.setMin(1);
        ratingSlider.setMax(5);
        ratingSlider.setValue(3);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);
        
        // Update rating label when slider changes
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rating = newVal.intValue();
            updateRatingLabel(rating);
        });
        
        // Initial rating label
        updateRatingLabel(3);
        
        // Setup table selection listener
        evaluationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEvaluation = newSelection;
                showEvaluationDetails(selectedEvaluation);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        
        // Initialize buttons state
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        
        // Load employees from the service
        loadEmployees();
        
        // Load performance evaluations
        refreshEvaluationList();
        
        // Register for employee events
        registerForEmployeeEvents();
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
        employeeList.addAll(performanceController.getAllEmployees());
        employeeComboBox.setItems(employeeList);
    }
    
    private void updateRatingLabel(int rating) {
        String description = switch (rating) {
            case 1 -> "Poor";
            case 2 -> "Below Average";
            case 3 -> "Average";
            case 4 -> "Good";
            case 5 -> "Excellent";
            default -> "Not Rated";
        };
        ratingLabel.setText(rating + " - " + description);
    }
    
    @FXML
    private void handleAddEvaluation() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            Employee selectedEmployee = employeeComboBox.getValue();
            if (selectedEmployee == null) {
                showNotification(NotificationSystem.Type.ERROR, "Please select an employee.");
                return;
            }
            
            LocalDate evaluationDate = evaluationDatePicker.getValue();
            int rating = (int) ratingSlider.getValue();
            String strengths = strengthsArea.getText().trim();
            String areasForImprovement = improvementArea.getText().trim();
            String comments = commentsArea.getText().trim();
            
            // Use controller to add evaluation
            boolean success = performanceController.addEvaluation(
                selectedEmployee.getId(),
                evaluationDate,
                rating,
                strengths,
                areasForImprovement,
                comments
            );
            
            if (success) {
                showNotification(NotificationSystem.Type.SUCCESS, "Performance evaluation added successfully.");
                clearForm();
                refreshEvaluationList();
            } else {
                showNotification(NotificationSystem.Type.ERROR, "Failed to add performance evaluation.");
            }
        } catch (Exception e) {
            showNotification(NotificationSystem.Type.ERROR, "An error occurred while adding the evaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleUpdateEvaluation() {
        if (selectedEvaluation == null) {
            showNotification(NotificationSystem.Type.WARNING, "Please select an evaluation to update.");
            return;
        }
        
        if (!validateInputs()) {
            return;
        }
        
        try {
            Employee selectedEmployee = employeeComboBox.getValue();
            if (selectedEmployee == null) {
                showNotification(NotificationSystem.Type.ERROR, "Please select an employee.");
                return;
            }
            
            LocalDate evaluationDate = evaluationDatePicker.getValue();
            int rating = (int) ratingSlider.getValue();
            String strengths = strengthsArea.getText().trim();
            String areasForImprovement = improvementArea.getText().trim();
            String comments = commentsArea.getText().trim();
            
            // Use controller to update evaluation
            boolean success = performanceController.updateEvaluation(
                selectedEvaluation.getId(),
                selectedEmployee.getId(),
                evaluationDate,
                rating,
                strengths,
                areasForImprovement,
                comments
            );
            
            if (success) {
                showNotification(NotificationSystem.Type.SUCCESS, "Performance evaluation updated successfully.");
                clearForm();
                refreshEvaluationList();
                evaluationTable.getSelectionModel().clearSelection();
            } else {
                showNotification(NotificationSystem.Type.ERROR, "Failed to update performance evaluation.");
            }
        } catch (Exception e) {
            showNotification(NotificationSystem.Type.ERROR, "An error occurred while updating the evaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeleteEvaluation() {
        if (selectedEvaluation == null) {
            showNotification(NotificationSystem.Type.WARNING, "Please select an evaluation to delete.");
            return;
        }
        
        // Custom confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Deletion");
        dialog.setHeaderText("Delete Performance Evaluation");
        dialog.setContentText("Are you sure you want to delete this performance evaluation?");
        
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButton, cancelButton);
        
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == deleteButton) {
                try {
                    // Use controller to delete evaluation
                    boolean success = performanceController.deleteEvaluation(selectedEvaluation.getId());
                    
                    if (success) {
                        showNotification(NotificationSystem.Type.SUCCESS, "Performance evaluation deleted successfully.");
                        clearForm();
                        refreshEvaluationList();
                        evaluationTable.getSelectionModel().clearSelection();
                    } else {
                        showNotification(NotificationSystem.Type.ERROR, "Failed to delete performance evaluation.");
                    }
                } catch (Exception e) {
                    showNotification(NotificationSystem.Type.ERROR, "An error occurred while deleting the evaluation: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    @FXML
    private void handleClearForm() {
        clearForm();
        evaluationTable.getSelectionModel().clearSelection();
    }
    
    public void refreshEvaluationList() {
        evaluationList.clear();
        evaluationList.addAll(performanceController.getAllEvaluations());
        evaluationTable.setItems(evaluationList);
    }
    
    private void showEvaluationDetails(PerformanceEvaluation evaluation) {
        // Find employee by ID
        Employee employee = performanceController.getEmployeeById(evaluation.getEmployeeId());
        employeeComboBox.setValue(employee);
        
        evaluationDatePicker.setValue(evaluation.getEvaluationDate());
        ratingSlider.setValue(evaluation.getPerformanceRating());
        updateRatingLabel(evaluation.getPerformanceRating());
        strengthsArea.setText(evaluation.getStrengths());
        improvementArea.setText(evaluation.getAreasForImprovement());
        commentsArea.setText(evaluation.getComments());
    }
    
    private void clearForm() {
        employeeComboBox.getSelectionModel().clearSelection();
        evaluationDatePicker.setValue(LocalDate.now());
        ratingSlider.setValue(3);
        updateRatingLabel(3);
        strengthsArea.clear();
        improvementArea.clear();
        commentsArea.clear();
        selectedEvaluation = null;
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (employeeComboBox.getValue() == null) {
            errorMessage.append("Please select an employee.\n");
        }
        
        if (evaluationDatePicker.getValue() == null) {
            errorMessage.append("Please select an evaluation date.\n");
        }
        
        if (strengthsArea.getText().trim().isEmpty()) {
            errorMessage.append("Please enter employee strengths.\n");
        }
        
        if (improvementArea.getText().trim().isEmpty()) {
            errorMessage.append("Please enter areas for improvement.\n");
        }
        
        if (errorMessage.length() > 0) {
            showNotification(NotificationSystem.Type.ERROR, errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    // Replace the old showAlert method with this notification method
    private void showNotification(NotificationSystem.Type type, String message) {
        if (notificationPane != null) {
            NotificationSystem.showNotification(notificationPane, message, type, 3);
        } else {
            // Fallback to console if notification pane not available
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
                loadEmployees();

            });
        });
        
        // Listen for employee updated events
        eventManager.addEventHandler(EmployeeEvent.EMPLOYEE_UPDATED, event -> {
            Platform.runLater(() -> {
                loadEmployees();
                // If the updated employee is currently selected, maintain the selection
                if (employeeComboBox.getValue() != null && 
                    employeeComboBox.getValue().getId().equals(event.getEmployee().getId())) {
                    for (Employee emp : employeeList) {
                        if (emp.getId().equals(event.getEmployee().getId())) {
                            employeeComboBox.setValue(emp);
                            break;
                        }
                    }
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
                }
            });
        });
    }
    
    // Called when the app shuts down
    public void shutdown() {
        // Any cleanup needed
    }
} 