package com.example.hrsm2.service;

import com.example.hrsm2.model.User;
import com.example.hrsm2.util.DatabaseDriver; // Import DatabaseDriver

// Remove map/list imports if no longer needed internally
// import java.util.ArrayList;
// import java.util.HashMap;
import java.util.List;
// import java.util.Map;

public class UserService {
    private static UserService instance;
    // private final Map<String, User> users = new HashMap<>(); // REMOVED - Use DB now
    private final DatabaseDriver dbDriver; // Instance of the DB driver

    // Current logged-in user
    private User currentUser;

    private UserService() {
        // Get the Singleton instance of DatabaseDriver
        this.dbDriver = DatabaseDriver.getInstance();
        // REMOVED - Super admin initialization is now handled by DatabaseDriver constructor
        /*
        User superAdmin = new User(
            "super",
            "super123",
            "Super Administrator",
            User.UserRole.SUPER_ADMIN
        );
        users.put(superAdmin.getUsername(), superAdmin);
        */
        // DatabaseDriver constructor now calls ensureSuperAdminExists()
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Authenticates a user against the database.
     * @param username The username entered by the user.
     * @param plainPassword The plain text password entered by the user.
     * @return The User object if authentication is successful, null otherwise.
     */
    public User authenticate(String username, String plainPassword) {
        User user = dbDriver.getUserByUsername(username); // Get user (with hashed password) from DB

        if (user != null) {
            // Hash the entered plain password using the same method as DB storage
            String enteredPasswordHash = dbDriver.hashPassword(plainPassword);

            // Compare the hash of the entered password with the stored hash
            if (enteredPasswordHash != null && enteredPasswordHash.equals(user.getPassword())) {
                currentUser = user; // Set current user on successful login
                return user;
            }
        }
        // Authentication failed (user not found or password mismatch)
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    /**
     * Creates a new user in the database.
     * @param username Username for the new user.
     * @param plainPassword Plain text password for the new user.
     * @param fullName Full name of the new user.
     * @param role Role of the new user.
     * @return true if creation was successful, false otherwise (e.g., username exists).
     */
    public boolean createUser(String username, String plainPassword, String fullName, User.UserRole role) {
        // Check if username already exists using the database
        if (isUsernameTaken(username)) {
            return false; // Username already exists
        }

        // Call DatabaseDriver to insert the user (it handles hashing)
        return dbDriver.insertUser(username, fullName, plainPassword, role.name());
    }

    /**
     * Updates an existing user in the database.
     * IMPORTANT: This implementation assumes you only update fullName and role.
     * Password updates would require more logic (e.g., a separate method or handling plain text).
     * If you need to update password, modify this method or add a specific one.
     *
     * @param user The User object with updated information (username identifies the user).
     *             The password in this object should be the EXISTING HASHED password unless changing it.
     * @return true if update was successful, false otherwise.
     */
    public boolean updateUser(User user) {
        // Basic check
        if (user == null || user.getUsername() == null) {
            return false;
        }
        // NOTE: This basic version calls dbDriver.updateUser which expects the HASHED password.
        // If you are only changing Full Name or Role, you MUST fetch the existing user first
        // to get their current hashed password and put it in the 'user' object being passed.
        // A more robust implementation would have specific methods like:
        // updateUserProfile(username, fullName, role)
        // changeUserPassword(username, newPlainPassword)

        // For simplicity, we assume the passed User object is fully populated correctly.
        // If the goal is just CRUD, and updates are rare/handled elsewhere, this might suffice.
        // However, if updating from a UI, fetching the current user first is safer:
        /*
         User existingUser = dbDriver.getUserByUsername(user.getUsername());
         if (existingUser == null) return false; // User not found

         // Create a user object to pass for update, keeping existing hash unless changing password
         User userToUpdate = new User(
             user.getUsername(),
             existingUser.getPassword(), // Keep existing hash by default
             user.getFullName(),       // Use new full name
             user.getRole()            // Use new role
         );
         // If password change is intended, hash the new plain password and set it here.

         return dbDriver.updateUser(userToUpdate);
        */

        // Simpler, direct call (assumes 'user' object has correct hashed password):
        return dbDriver.updateUser(user);
    }

    public boolean deleteUser(String username) {
        // Don't allow deleting the currently logged-in user
        if (currentUser != null && currentUser.getUsername().equals(username)) {
            System.err.println("Cannot delete the currently logged-in user.");
            return false;
        }
        // Prevent deleting the super admin account
        if ("super".equalsIgnoreCase(username)) {
            System.err.println("Cannot delete the default super admin account.");
            return false;
        }

        return dbDriver.deleteUser(username);
    }

    /**
     * Retrieves all users from the database.
     * @return A List of all User objects.
     */
    public List<User> getAllUsers() {
        return dbDriver.getAllUsers();
    }


    public User getUserByUsername(String username) {
        return dbDriver.getUserByUsername(username);
    }


    public boolean isUsernameTaken(String username) {
        return dbDriver.getUserByUsername(username) != null;
    }
}


// ely f baly sa7