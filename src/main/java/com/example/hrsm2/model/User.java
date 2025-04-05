package com.example.hrsm2.model;

// No need for UUID import anymore
// import java.util.UUID;

public class User {
    // private String id; // REMOVED - username is the primary key in DB
    private String username;
    private String password; // This will store the HASHED password when retrieved from DB
    private String fullName;
    private UserRole role;

    public enum UserRole {
        SUPER_ADMIN,  // Can create HR accounts
        HR_ADMIN      // Regular system user
    }

    // Main constructor used when creating a new user object *before* saving
    // or when mapping from the database result set.
    public User(String username, String password, String fullName, UserRole role) {
        // this.id = UUID.randomUUID().toString(); // REMOVED
        this.username = username;
        this.password = password; // Store whatever password is passed (plain or hash)
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password; // Returns the password stored in the object (could be plain or hash)
    }

    public void setPassword(String password) {
        this.password = password; // Allows setting (e.g., before hashing or updating)
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