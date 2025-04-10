package com.example.hrsm2.gui;

import com.example.hrsm2.controller.UserController;
import com.example.hrsm2.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

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
    
    @FXML
    private StackPane notificationPane;

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
            showNotification(NotificationSystem.Type.ERROR, validationError);
            return;
        }

        // Create new HR user using controller
        boolean success = userController.createHrAdminUser(username, plainPassword, fullName);

        if (success) {
            showNotification(NotificationSystem.Type.SUCCESS, "HR user created successfully.");
            clearForm();
            refreshUserList();
        } else {
            // Check if the reason was username taken
            if (userController.isUsernameTaken(username)) {
                showNotification(NotificationSystem.Type.ERROR, "Username '" + username + "' already exists. Please choose a different username.");
            } else {
                showNotification(NotificationSystem.Type.ERROR, "Failed to create HR user. See console/logs for details.");
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
            showNotification(NotificationSystem.Type.WARNING, "The default super administrator account cannot be deleted.");
            return;
        }

        // Use Dialog for important confirmation
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Delete");
        dialog.setHeaderText("Delete User");
        dialog.setContentText("Are you sure you want to delete the user: " + selectedUser.getUsername() + "?");
        
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButton, cancelButton);

        dialog.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                // Call controller to delete user
                boolean success = userController.deleteUser(selectedUser.getUsername());

                if (success) {
                    showNotification(NotificationSystem.Type.SUCCESS, "User deleted successfully.");
                    refreshUserList();
                } else {
                    showNotification(NotificationSystem.Type.ERROR, "Failed to delete user. The user might be the current user or an error occurred.");
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

    private void showNotification(NotificationSystem.Type type, String message) {
        if (notificationPane != null) {
            NotificationSystem.showNotification(notificationPane, message, type, 4);
        } else {
            // Fallback to console if notification pane not available
            System.out.println(type + ": " + message);
        }
    }
} 