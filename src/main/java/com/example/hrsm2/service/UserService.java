package com.example.hrsm2.service;

import com.example.hrsm2.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private static UserService instance;
    private final Map<String, User> users = new HashMap<>();
    
    // Current logged-in user
    private User currentUser;
    
    private UserService() {
        // Initialize with a super admin account
        User superAdmin = new User(
            "super", 
            "super123", 
            "Super Administrator", 
            User.UserRole.SUPER_ADMIN
        );
        users.put(superAdmin.getUsername(), superAdmin);
    }
    
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            // Set as current user
            currentUser = user;
            return user;
        }
        return null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public boolean createUser(String username, String password, String fullName, User.UserRole role) {
        // Check if username already exists
        if (users.containsKey(username)) {
            return false;
        }
        
        // Create new user
        User newUser = new User(username, password, fullName, role);
        users.put(username, newUser);
        return true;
    }
    
    public boolean updateUser(User user) {
        if (users.containsKey(user.getUsername())) {
            users.put(user.getUsername(), user);
            return true;
        }
        return false;
    }
    
    public boolean deleteUser(String username) {
        // Don't allow deleting current user
        if (currentUser != null && currentUser.getUsername().equals(username)) {
            return false;
        }
        
        return users.remove(username) != null;
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public User getUserByUsername(String username) {
        return users.get(username);
    }
    
    public boolean isUsernameTaken(String username) {
        return users.containsKey(username);
    }
} 