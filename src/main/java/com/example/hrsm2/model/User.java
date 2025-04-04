package com.example.hrsm2.model;

import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private UserRole role;
    
    public enum UserRole {
        SUPER_ADMIN,  // Can create HR accounts
        HR_ADMIN      // Regular system user
    }
    
    public User(String username, String password, String fullName, UserRole role) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
    
    // Constructor with explicit ID (for loading from storage)
    public User(String id, String username, String password, String fullName, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }
    
    public boolean isHrAdmin() {
        return role == UserRole.HR_ADMIN;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
} 