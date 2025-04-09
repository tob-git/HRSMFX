package com.example.hrsm2.controller;

import com.example.hrsm2.HRMSApplication;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.EmployeeService;
import com.example.hrsm2.service.LeaveRequestService;
import com.example.hrsm2.service.PayrollService;
import com.example.hrsm2.service.PerformanceEvaluationService;
import com.example.hrsm2.service.UserService;
import com.example.hrsm2.gui.EmployeeGUI;
import com.example.hrsm2.gui.PayrollGUI;
import com.example.hrsm2.gui.LeaveGUI;
import com.example.hrsm2.gui.PerformanceGUI;
import com.example.hrsm2.gui.UserGUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private TabPane tabPane;
    
    @FXML
    private BorderPane mainBorderPane;
    
    // Service instances
    private final EmployeeService employeeService = EmployeeService.getInstance();
    private final LeaveRequestService leaveRequestService = LeaveRequestService.getInstance();
    private final PayrollService payrollService = PayrollService.getInstance();
    private final PerformanceEvaluationService performanceEvaluationService = PerformanceEvaluationService.getInstance();
    private final UserService userService = UserService.getInstance();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {

            
            // Check if current user is a super admin and add user management tab if so
            User currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.isSuperAdmin()) {
                loadUserManagementTab();
            }
            else {
                // Load standard tabs
                loadEmployeeTab();
                loadLeaveManagementTab();
                loadPayrollTab();
                loadPerformanceTab();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize application: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Log out the current user
            userService.logout();
            
            // Load login view
            FXMLLoader loader = new FXMLLoader(HRMSApplication.class.getResource("login-view.fxml"));
            Scene loginScene = new Scene(loader.load(), 600, 400);
            
            // Get current stage
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            
            // Set new scene
            stage.setTitle("HRMS Login");
            stage.setScene(loginScene);
            stage.setMaximized(false);
            stage.show();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About HRMS");
        alert.setHeaderText("Human Resource Management System");
        alert.setContentText("Version 1.0\nDeveloped with JavaFX\n© 2023 HRMS Application");
        alert.showAndWait();
    }
    
    private void loadEmployeeTab() throws IOException {
        Tab employeeTab = new Tab("Employee Management");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hrsm2/employee-view.fxml"));
        Parent employeeView = loader.load();
        EmployeeGUI controller = loader.getController();
        employeeTab.setContent(employeeView);
        tabPane.getTabs().add(employeeTab);
    }
    
    private void loadLeaveManagementTab() throws IOException {
        Tab leaveTab = new Tab("Leave Management");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hrsm2/leave-view.fxml"));
        Parent leaveView = loader.load();
        LeaveGUI controller = loader.getController();
        leaveTab.setContent(leaveView);
        tabPane.getTabs().add(leaveTab);
    }
    
    private void loadPayrollTab() throws IOException {
        Tab payrollTab = new Tab("Payroll Processing");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hrsm2/payroll-view.fxml"));
        Parent payrollView = loader.load();
        PayrollGUI controller = loader.getController();
        payrollTab.setContent(payrollView);
        tabPane.getTabs().add(payrollTab);
    }
    
    private void loadPerformanceTab() throws IOException {
        Tab performanceTab = new Tab("Performance Evaluations");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hrsm2/performance-view.fxml"));
        Parent performanceView = loader.load();
        PerformanceGUI controller = loader.getController();
        performanceTab.setContent(performanceView);
        tabPane.getTabs().add(performanceTab);
    }
    
    private void loadUserManagementTab() throws IOException {
        Tab userTab = new Tab("User Management");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hrsm2/user-view.fxml"));
        Parent userView = loader.load();
        UserGUI controller = loader.getController();
        userTab.setContent(userView);
        tabPane.getTabs().add(userTab);
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 