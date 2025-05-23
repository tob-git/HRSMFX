package com.example.hrsm2.gui;

import com.example.hrsm2.HRMSApplication;
import com.example.hrsm2.controller.LoginController;
import com.example.hrsm2.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * GUI class for login screen.
 * Handles UI elements and user interactions for login.
 */
public class LoginGUI {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    // Controller for business logic
    private final LoginController loginController = new LoginController();
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Use controller for authentication
        User user = loginController.authenticate(username, password);
        
        if (user != null) {
            try {
                // Load main view
                FXMLLoader loader = new FXMLLoader(HRMSApplication.class.getResource("main-view.fxml"));
                Scene mainScene = new Scene(loader.load(), 1000, 700);
                
                // Get current stage
                Stage stage = (Stage) loginButton.getScene().getWindow();
                
                // Set new scene
                stage.setTitle("Human Resource Management System");
                stage.setScene(mainScene);
                stage.setMaximized(false);
                stage.show();
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load main view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Invalid username or password.");
        }
    }
    
    /**
     * Display an alert dialog
     * @param alertType The type of alert
     * @param title The alert title
     * @param content The alert content
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 