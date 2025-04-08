package com.example.hrsm2.controller;

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
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PerformanceController implements Initializable {
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
    
    private final EmployeeService employeeService = EmployeeService.getInstance();
    private final PerformanceEvaluationService evaluationService = PerformanceEvaluationService.getInstance();
    private ObservableList<PerformanceEvaluation> evaluationList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private PerformanceEvaluation selectedEvaluation;
    private final UserService userService = UserService.getInstance();
    User currentUser = userService.getCurrentUser();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
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
        employeeList.addAll(employeeService.getAllEmployees());
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
        try {
            // Validate input data
            if (!validateInputs()) {
                return;
            }
            
            Employee selectedEmployee = employeeComboBox.getValue();
            int rating = (int) ratingSlider.getValue();
            String strengths = strengthsArea.getText();
            String improvement = improvementArea.getText();
            String comments = commentsArea.getText();
            String reviewedBy = currentUser.getFullName();
            
            // Create new evaluation
            PerformanceEvaluation evaluation = new PerformanceEvaluation(
                selectedEmployee.getId(),
                rating,
                strengths,
                improvement,
                comments,
                reviewedBy
            );
            
            // Set custom evaluation date if provided
            if (evaluationDatePicker.getValue() != null) {
                evaluation.setEvaluationDate(evaluationDatePicker.getValue());
            }
            
            // Add to service
            evaluationService.addEvaluation(evaluation);
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Performance evaluation added successfully.");
            
            // Clear form and refresh list
            clearForm();
            refreshEvaluationList();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add evaluation: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateEvaluation() {
        if (selectedEvaluation == null) {
            return;
        }
        
        try {
            // Validate input data
            if (!validateInputs()) {
                return;
            }
            
            // Update evaluation data
            Employee selectedEmployee = employeeComboBox.getValue();
            selectedEvaluation.setEmployeeId(selectedEmployee.getId());
            selectedEvaluation.setEvaluationDate(evaluationDatePicker.getValue());
            selectedEvaluation.setPerformanceRating((int) ratingSlider.getValue());
            selectedEvaluation.setStrengths(strengthsArea.getText());
            selectedEvaluation.setAreasForImprovement(improvementArea.getText());
            selectedEvaluation.setComments(commentsArea.getText());
            selectedEvaluation.setReviewedBy(currentUser.getFullName());
            
            // Update in service
            evaluationService.updateEvaluation(selectedEvaluation);
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Performance evaluation updated successfully.");
            
            // Refresh the table
            refreshEvaluationList();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update evaluation: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteEvaluation() {
        if (selectedEvaluation == null) {
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Performance Evaluation");
        confirmAlert.setContentText("Are you sure you want to delete this evaluation?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete from service
                evaluationService.deleteEvaluation(selectedEvaluation.getId());
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Performance evaluation deleted successfully.");
                
                // Clear form and refresh list
                clearForm();
                refreshEvaluationList();
                
                // Reset selection
                selectedEvaluation = null;
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }
    
    @FXML
    private void handleClearForm() {
        clearForm();
        evaluationTable.getSelectionModel().clearSelection();
        selectedEvaluation = null;
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    public void refreshEvaluationList() {
        evaluationList.clear();
        evaluationList.addAll(evaluationService.getAllEvaluations());
        evaluationTable.setItems(evaluationList);
    }
    
    private void showEvaluationDetails(PerformanceEvaluation evaluation) {
        // Find employee by ID
        for (Employee employee : employeeList) {
            if (employee.getId().equals(evaluation.getEmployeeId())) {
                employeeComboBox.setValue(employee);
                break;
            }
        }
        
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
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (employeeComboBox.getValue() == null) {
            errorMessage.append("Employee is required.\n");
        }
        
        if (evaluationDatePicker.getValue() == null) {
            errorMessage.append("Evaluation date is required.\n");
        }
        
        if (strengthsArea.getText().trim().isEmpty()) {
            errorMessage.append("Strengths is required.\n");
        }
        
        if (improvementArea.getText().trim().isEmpty()) {
            errorMessage.append("Areas for improvement is required.\n");
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
                // If the updated employee is currently selected, keep the selection
                if (employeeComboBox.getValue() != null && 
                    employeeComboBox.getValue().getId().equals(event.getEmployee().getId())) {
                    // The employee object in the combo box will be updated by loadEmployees()
                    // No need to do anything else
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
} 