package com.example.hrsm2.controller;

import com.example.hrsm2.model.User;
import com.example.hrsm2.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    @FXML
    private TableView<User> userTable;
    
    @FXML
    private TableColumn<User, String> usernameColumn;
    
    @FXML
    private TableColumn<User, String> fullNameColumn;
    
    @FXML
    private TableColumn<User, User.UserRole> roleColumn;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button deleteButton;
    
    private final UserService userService = UserService.getInstance();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Setup table selection listener
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                deleteButton.setDisable(false);
            } else {
                selectedUser = null;
                deleteButton.setDisable(true);
            }
        });
        
        // Initialize button states
        deleteButton.setDisable(true);
        
        // Load initial user data
        refreshUserList();
    }
    
    private void refreshUserList() {
        userList.clear();
        
        // Filter to only show HR_ADMIN users for the super admin
        userService.getAllUsers().forEach(user -> {
            if (user.getRole() == User.UserRole.HR_ADMIN) {
                userList.add(user);
            }
        });
        
        userTable.setItems(userList);
    }
    
    @FXML
    private void handleAddUser() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String fullName = fullNameField.getText().trim();
        
        // Create new HR user
        boolean success = userService.createUser(username, password, fullName, User.UserRole.HR_ADMIN);
        
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "HR user created successfully.");
            clearForm();
            refreshUserList();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Username already exists. Please choose a different username.");
        }
    }
    
    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete the user: " + selectedUser.getUsername() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = userService.deleteUser(selectedUser.getUsername());
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
                    refreshUserList();
                    clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
                }
            }
        });
    }
    
    @FXML
    private void handleClearForm() {
        clearForm();
    }
    
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        fullNameField.clear();
        userTable.getSelectionModel().clearSelection();
        selectedUser = null;
        deleteButton.setDisable(true);
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        
        if (username.isEmpty()) {
            errorMessage.append("Username is required.\n");
        }
        
        if (password.isEmpty()) {
            errorMessage.append("Password is required.\n");
        }
        
        if (!password.equals(confirmPassword)) {
            errorMessage.append("Passwords do not match.\n");
        }
        
        if (fullName.isEmpty()) {
            errorMessage.append("Full name is required.\n");
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