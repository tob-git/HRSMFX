package com.example.hrsm2.controller;

import com.example.hrsm2.model.User;
import com.example.hrsm2.service.UserService;

/**
 * Controller class for authentication and login operations.
 * Contains business logic for user authentication.
 */
public class LoginController {
    // Service
    private final UserService userService;
    
    /**
     * Constructor initializes the user service
     */
    public LoginController() {
        this.userService = UserService.getInstance();
    }
    
    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String username, String password) {
        return userService.authenticate(username, password);
    }
    
    /**
     * Check if a user is a super admin
     * @param user The user to check
     * @return true if user is a super admin, false otherwise
     */
    public boolean isSuperAdmin(User user) {
        return user != null && user.isSuperAdmin();
    }
    
    /**
     * Get the current authenticated user
     * @return The current authenticated user
     */
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }
    
    /**
     * Logout the current user
     */
    public void logout() {
        userService.logout();
    }
} 