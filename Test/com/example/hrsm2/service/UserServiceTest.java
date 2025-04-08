package com.example.hrsm2.service;

import com.example.hrsm2.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

// Use test method ordering based on @Order annotations
@TestMethodOrder(OrderAnnotation.class)
class UserServiceTest {

    private UserService operation;
    private User user;

    @BeforeEach
    void setUp() {
        // Get the singleton instance of UserService
        operation = UserService.getInstance();

        // Create a default test user
        user = new User("user", "test123", "Test User", User.UserRole.HR_ADMIN);

        // Clean up in case user already exists
        operation.deleteUser(user.getUsername());
    }

    @AfterEach
    void tearDown() {
        // Ensure the user is logged out before attempting deletion
        if (operation.getCurrentUser() != null) {
            operation.logout(); // Log out if the user is still logged in
        }

        // Remove the test user after ensuring logout
        operation.deleteUser(user.getUsername());
    }

    @Test
    @Order(1)
    @DisplayName("1. Should return same instance for UserService singleton")
    void testGetInstance() {
        UserService anotherInstance = UserService.getInstance();
        assertSame(operation, anotherInstance); // Verify singleton behavior
    }

    @Test
    @Order(2)
    @DisplayName("2. Should successfully create a new user")
    void testCreateUser() {
        boolean created = operation.createUser("user", "test123", "Test User", User.UserRole.HR_ADMIN);
        assertTrue(created);
        assertTrue(operation.isUsernameTaken("user"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Should retrieve user by username")
    void testGetUserByUsername() {
        operation.createUser("user", "test123", "Test User", User.UserRole.HR_ADMIN);
        User user = operation.getUserByUsername("user");
        assertNotNull(user);
        assertEquals("Test User", user.getFullName());
    }

    @Test
    @Order(4)
    @DisplayName("4. Should return true/false for existing and non-existing usernames")
    void testIsUsernameTaken() {
        assertTrue(operation.isUsernameTaken("super"));         // Already exists
        assertFalse(operation.isUsernameTaken("fadij1234"));    // Doesn't exist
        assertTrue(operation.isUsernameTaken("miky2004"));      // Already exists
    }

    @Test
    @Order(5)
    @DisplayName("5. Should authenticate a user with correct credentials")
    void testAuthenticate() {
        operation.createUser(user.getUsername(), user.getPassword(), user.getFullName(), user.getRole());
        User authenticated = operation.authenticate("user", "test123");

        assertNotNull(authenticated);
        assertEquals("user", authenticated.getUsername());
    }

    @Test
    @Order(6)
    @DisplayName("6. Should get the currently logged-in user")
    void testGetCurrentUser() {
        operation.createUser(user.getUsername(), user.getPassword(), user.getFullName(), user.getRole());
        operation.authenticate("user", "test123");

        User current = operation.getCurrentUser();
        assertNotNull(current);
        assertEquals("user", current.getUsername());
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("7. Should logout user and clear current session")
    @CsvSource({
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi"
    })
    void testLogout(String username, String password, String FullName) {
        operation.createUser(username, password, FullName, User.UserRole.HR_ADMIN);
        operation.authenticate(username, password);

        operation.logout();
        assertNull(operation.getCurrentUser(), "User should be null after logout");
    }

    @Test
    @Order(8)
    @DisplayName("8. Should update user information")
    void testUpdateUser() {
        operation.createUser("user", "test123", "Test User", User.UserRole.HR_ADMIN);

        User existing = operation.getUserByUsername("user");
        User updatedUser = new User(existing.getUsername(), existing.getPassword(), "Updated Name", User.UserRole.SUPER_ADMIN);

        boolean updated = operation.updateUser(updatedUser);
        assertTrue(updated);

        User fromDb = operation.getUserByUsername("user");
        assertEquals("Updated Name", fromDb.getFullName());
        assertEquals(User.UserRole.SUPER_ADMIN, fromDb.getRole());
    }

    @Test
    @Order(9)
    @DisplayName("9. Should delete a user and verify deletion (after logout)")
    void testDeleteUser() {
        // Create and authenticate user
        operation.createUser("user", "test123", "Test User", User.UserRole.HR_ADMIN);
        operation.authenticate("user", "test123");

        // Ensure user is logged in before logout
        assertNotNull(operation.getCurrentUser(), "User should be logged in before deletion");

        // Logout before deletion
        operation.logout();
        assertNull(operation.getCurrentUser(), "User should be logged out before deletion");

        // Delete user
        assertTrue(operation.deleteUser("user"), "User deletion should return true");
        assertFalse(operation.isUsernameTaken("user"), "Username should not be taken after deletion");
    }
}
