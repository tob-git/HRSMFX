package com.example.hrsm2.controller;

import com.example.hrsm2.model.User;
import com.example.hrsm2.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains business logic for managing users.
 */
public class UserController {
    // Service for user operations
    private final UserService userService;
    
    /**
     * Constructor for UserController.
     */
    public UserController() {
        this.userService = UserService.getInstance();
    }
    
    /**
     * Gets all HR_ADMIN users from the user service.
     * 
     * @return a list of all HR_ADMIN users
     */
    public List<User> getAllHrAdminUsers() {
        List<User> allUsers = userService.getAllUsers();
        return allUsers.stream()
                .filter(user -> user.getRole() == User.UserRole.HR_ADMIN)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a new HR_ADMIN user.
     * 
     * @param username the username
     * @param plainPassword the plain text password
     * @param fullName the full name
     * @return true if the user was created successfully, false otherwise
     */
    public boolean createHrAdminUser(String username, String plainPassword, String fullName) {
        return userService.createUser(username, plainPassword, fullName, User.UserRole.HR_ADMIN);
    }
    
    /**
     * Checks if a username is already taken.
     * 
     * @param username the username to check
     * @return true if the username is taken, false otherwise
     */
    public boolean isUsernameTaken(String username) {
        return userService.isUsernameTaken(username);
    }
    
    /**
     * Deletes a user by username.
     * 
     * @param username the username of the user to delete
     * @return true if the user was deleted successfully, false otherwise
     */
    public boolean deleteUser(String username) {
        return userService.deleteUser(username);
    }
    
    /**
     * Validates user input data.
     * 
     * @param username the username
     * @param password the password
     * @param confirmPassword the password confirmation
     * @param fullName the full name
     * @param selectedUser the currently selected user (for update operations)
     * @return an error message, or an empty string if validation passes
     */
    public String validateUserInputs(String username, String password, String confirmPassword, String fullName, User selectedUser) {
        StringBuilder errorMessage = new StringBuilder();

        if (username.isEmpty()) {
            errorMessage.append("Username is required.\n");
        } else if (username.equals("super") && (selectedUser == null || !selectedUser.getUsername().equals("super"))) {
            // Prevent creating another user named "super" (case-sensitive check)
            errorMessage.append("Username \\'super\\' is reserved.\n");
        }

        if (password.isEmpty()) {
            errorMessage.append("Password is required.\n");
        } else if (password.length() < 6) {
            errorMessage.append("Password must be at least 6 characters long.\n");
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.append("Passwords do not match.\n");
        }

        if (fullName.isEmpty()) {
            errorMessage.append("Full name is required.\n");
        }

        return errorMessage.toString();
    }
}