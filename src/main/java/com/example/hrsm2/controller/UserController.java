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
import java.util.List;
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
    private PasswordField passwordField; // Input for plain text password

    @FXML
    private PasswordField confirmPasswordField; // Input for plain text password confirmation

    @FXML
    private TextField fullNameField;

    @FXML
    private Button addButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button deleteButton;

    // UserService now interacts with the database
    private final UserService userService = UserService.getInstance();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser; // This will hold the User object fetched from the DB via UserService

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns (no changes needed)
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Setup table selection listener (no changes needed)
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection; // selectedUser now holds a DB-backed user object
                deleteButton.setDisable(false);
                // Optionally populate fields for editing (if update functionality is added)
                // usernameField.setText(selectedUser.getUsername()); - Usually don't edit username
                // fullNameField.setText(selectedUser.getFullName());
                // passwordField.clear(); // Don't show hash
                // confirmPasswordField.clear();
            } else {
                selectedUser = null;
                deleteButton.setDisable(true);
                // Optionally clear fields if deselected
                clearForm(); // Clear form when selection is cleared
            }
        });

        // Initialize button states (no changes needed)
        deleteButton.setDisable(true);

        // Load initial user data from the database via UserService
        refreshUserList();
    }

    // Refreshes the list from the database
    public void refreshUserList() {
        userList.clear();

        // Get all users from the service (which now gets them from the DB)
        List<User> allUsersFromDb = userService.getAllUsers();

        // Filter to only show HR_ADMIN users for the super admin (or adjust as needed)
        allUsersFromDb.forEach(user -> {
            if (user.getRole() == User.UserRole.HR_ADMIN) {
                userList.add(user);
            }
            // If you want the Super Admin to see themselves:
            // if (user.getRole() == User.UserRole.HR_ADMIN || user.isSuperAdmin()) {
            //    userList.add(user);
            // }
        });

        userTable.setItems(userList);
        // Ensure selection is cleared after refresh if needed
        // userTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAddUser() {
        // Validate inputs (no changes needed)
        if (!validateInputs()) {
            return;
        }

        String username = usernameField.getText().trim();
        String plainPassword = passwordField.getText(); // Get plain text password
        String fullName = fullNameField.getText().trim();

        // Create new HR user - UserService now handles hashing and DB insertion
        boolean success = userService.createUser(username, plainPassword, fullName, User.UserRole.HR_ADMIN);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "HR user created successfully.");
            clearForm();
            refreshUserList(); // Reload list from DB
        } else {
            // Check if the reason was username taken
            if (userService.isUsernameTaken(username)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Username '" + username + "' already exists. Please choose a different username.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create HR user. See console/logs for details.");
            }
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            return; // Should not happen if button is enabled correctly
        }

        // Prevent deleting the super admin through the UI (already handled in service, but good UI feedback)
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
                // Call UserService to delete from DB
                boolean success = userService.deleteUser(selectedUser.getUsername());

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
                    refreshUserList(); // Reload list from DB
                    // clearForm(); // Already cleared by selection listener or can be called explicitly
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
        userTable.getSelectionModel().clearSelection(); // This will trigger the listener, setting selectedUser to null
        // selectedUser = null; // Handled by listener
        // deleteButton.setDisable(true); // Handled by listener
    }

    // Validation logic remains the same
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();

        if (username.isEmpty()) {
            errorMessage.append("Username is required.\n");
        } else if (username.equalsIgnoreCase("super") && selectedUser == null) {
            // Prevent creating another user named "super" (case-insensitive check)
            // Allow editing if 'super' is selectedUser (though updates might be restricted elsewhere)
            errorMessage.append("Username 'super' is reserved.\n");
        }


        if (password.isEmpty()) {
            errorMessage.append("Password is required.\n");
        } else if (password.length() < 6) { // Example: Add password complexity rule
            errorMessage.append("Password must be at least 6 characters long.\n");
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

    // Alert utility remains the same
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}