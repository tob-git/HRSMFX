package com.example.hrsm2.gui;

import com.example.hrsm2.controller.UserController;
import com.example.hrsm2.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserGUI implements Initializable {
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

    // Controller for business logic
    private final UserController userController = new UserController();
    
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
                clearForm();
            }
        });

        // Initialize button states
        deleteButton.setDisable(true);

        // Load initial user data
        refreshUserList();
    }

    // Refreshes the list from the database
    public void refreshUserList() {
        userList.clear();
        userList.addAll(userController.getAllHrAdminUsers());
        userTable.setItems(userList);
    }

    @FXML
    private void handleAddUser() {
        String username = usernameField.getText().trim();
        String plainPassword = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();

        // Validate inputs using controller
        String validationError = userController.validateUserInputs(
            username, plainPassword, confirmPassword, fullName, selectedUser);
        
        if (!validationError.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", validationError);
            return;
        }

        // Create new HR user using controller
        boolean success = userController.createHrAdminUser(username, plainPassword, fullName);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "HR user created successfully.");
            clearForm();
            refreshUserList();
        } else {
            // Check if the reason was username taken
            if (userController.isUsernameTaken(username)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Username '" + username + "' already exists. Please choose a different username.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create HR user. See console/logs for details.");
            }
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            return;
        }

        // Prevent deleting the super admin through the UI
        if ("super".equalsIgnoreCase(selectedUser.getUsername())) {
            showAlert(Alert.AlertType.WARNING, "Delete Denied", "The default super administrator account cannot be deleted.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete the user: " + selectedUser.getUsername() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Call controller to delete user
                boolean success = userController.deleteUser(selectedUser.getUsername());

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
                    refreshUserList();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user. The user might be the current user or an error occurred.");
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
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 