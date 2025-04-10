package com.example.hrsm2.gui;

import com.example.hrsm2.HRMSApplication;
import com.example.hrsm2.controller.LoginController;
import com.example.hrsm2.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
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
    
    @FXML
    private StackPane notificationPane;
    
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
                showNotification(NotificationSystem.Type.ERROR, "Failed to load main view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showNotification(NotificationSystem.Type.ERROR, "Invalid username or password.");
        }
    }
    
    /**
     * Display a notification
     */
    private void showNotification(NotificationSystem.Type type, String message) {
        if (notificationPane != null) {
            NotificationSystem.showNotification(notificationPane, message, type, 4);
        } else {
            // Fallback to console if notification pane not available
            System.out.println(type + ": " + message);
        }
    }
} 