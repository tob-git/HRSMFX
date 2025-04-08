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

    private static UserService operation;
    private User user;

//    @AfterAll
//    static void Create() {
//        operation.createUser("firo", "firo123", "Farid Fahmi", User.UserRole.HR_ADMIN);
//        operation.createUser("fadij", "fadij123", "Fady John", User.UserRole.HR_ADMIN);
//        operation.createUser("miky2004", "miky123", "Michael Nagi", User.UserRole.HR_ADMIN);
//        operation.createUser("TopG", "TopG123", "Mohamed Khaled", User.UserRole.HR_ADMIN);
//    }

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

    @ParameterizedTest
    @Order(2)
    @DisplayName("2. Should successfully create a new user")
    @CsvSource({
            "firo, firo123, Farid Fahmi",
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi",
            "TopG, TopG123, Mohamed Khaled"
    })
    void testCreateUser(String username, String password, String FullName) {
        boolean created = operation.createUser(username, password, FullName, User.UserRole.HR_ADMIN);
        assertTrue(created);
        assertTrue(operation.isUsernameTaken(username));
    }

    @ParameterizedTest
    @Order(3)
    @DisplayName("3. Should Fail creating an existing user")
    @CsvSource({
            "firo, firo123, Farid Fahmi",
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi",
            "TopG, TopG123, Mohamed Khaled"
    })
    void testCreateExistingUser(String username, String password, String FullName) {
        boolean exist = operation.createUser(username, password, FullName, User.UserRole.HR_ADMIN);
        assertFalse(exist);
        assertTrue(operation.isUsernameTaken(username));
    }

    @ParameterizedTest
    @Order(4)
    @DisplayName("4. Should retrieve user by username")
    @CsvSource({
            "firo, firo123, Farid Fahmi",
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi",
            "TopG, TopG123, Mohamed Khaled"
    })
    void testGetUserByUsername(String username, String password, String FullName) {
        operation.createUser(username, password, FullName, User.UserRole.HR_ADMIN);
        User user = operation.getUserByUsername(username);
        assertNotNull(user);
        assertEquals(FullName, user.getFullName());
    }

    @ParameterizedTest
    @Order(5)
    @DisplayName("5. Should return true/false for existing and non-existing usernames")
    @CsvSource({
            "super, super123",
            "fadij, fadij123",
            "miky2004, miky123",
            "firo, firo123",
            "TopG, TopG123"
    })
    void testIsUsernameTaken(String username_true, String username_false) {
        assertTrue(operation.isUsernameTaken(username_true));         // Already exists
        assertFalse(operation.isUsernameTaken(username_false));       // Doesn't exist
    }

    @ParameterizedTest
    @Order(6)
    @DisplayName("6. Should authenticate a user with correct credentials")
    @CsvSource({
            "super, super123",
            "fadij, fadij123",
            "miky2004, miky123",
            "firo, firo123",
            "TopG, TopG123"
    })
    void testCorrectAuthenticate(String username, String password) {
        operation.createUser(user.getUsername(), user.getPassword(), user.getFullName(), user.getRole());
        User authenticated = operation.authenticate(username, password);

        assertNotNull(authenticated);
        assertEquals(username, authenticated.getUsername());
    }

    @ParameterizedTest
    @Order(7)
    @DisplayName("7. Should not authenticate a user with incorrect credentials")
    @CsvSource({
            "super1, super123",
            "fadij, fadij1231",
            "miky2004, miky1231",
            "firo1, firo123",
            "TopG, TopG1231"
    })
    void testIncorrectAuthenticate(String username, String password) {
        operation.createUser(user.getUsername(), user.getPassword(), user.getFullName(), user.getRole());
        User notAuthenticated = operation.authenticate(username, password);

        assertNull(notAuthenticated);
    }

    @ParameterizedTest
    @Order(8)
    @DisplayName("8. Should get the currently logged-in user")
    @CsvSource({
            "super, super123",
            "fadij, fadij123",
            "miky2004, miky123",
            "firo, firo123",
            "TopG, TopG123"
    })
    void testGetCurrentUser(String username, String password) {
        operation.createUser(user.getUsername(), user.getPassword(), user.getFullName(), user.getRole());
        operation.authenticate(username, password);

        User current = operation.getCurrentUser();
        assertNotNull(current);
        assertEquals(username, current.getUsername());
    }

    @ParameterizedTest
    @Order(9)
    @DisplayName("9. Should logout user and clear current session")
    @CsvSource({
            "firo, firo123, Farid Fahmi",
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi",
            "TopG, TopG123, Mohamed Khaled"
    })
    void testLogout(String username, String password, String FullName) {
        operation.createUser(username, password, FullName, User.UserRole.HR_ADMIN);
        operation.authenticate(username, password);

        operation.logout();
        assertNull(operation.getCurrentUser(), "User should be null after logout");
    }

    @ParameterizedTest
    @Order(10)
    @DisplayName("10. Should update users information")
    @CsvSource({
            "firo, firo123, Farid Fahmi",
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi",
            "TopG, TopG123, Mohamed Khaled"
    })
    void testUpdateUser(String username, String password, String FullName) {
        operation.createUser(username, password, FullName, User.UserRole.HR_ADMIN);

        User existing = operation.getUserByUsername(username);
        User updatedUser = new User(existing.getUsername(), existing.getPassword(), "Updated Name", User.UserRole.SUPER_ADMIN);

        boolean updated = operation.updateUser(updatedUser);
        assertTrue(updated);

        User fromDb = operation.getUserByUsername(username);
        assertEquals("Updated Name", fromDb.getFullName());
        assertEquals(User.UserRole.SUPER_ADMIN, fromDb.getRole());
    }

    @ParameterizedTest
    @Order(11)
    @DisplayName("11. Should delete a user and verify deletion (after logout)")
    @CsvSource({
            "firo, firo123, Farid Fahmi",
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi",
            "TopG, TopG123, Mohamed Khaled"
    })
    void testDeleteUser(String username, String password, String FullName) {
        // Create and authenticate user
        operation.createUser(username, password, FullName, User.UserRole.HR_ADMIN);
        operation.authenticate(username, password);

        // Ensure user is logged in before logout
        assertNotNull(operation.getCurrentUser(), "User should be logged in before deletion");

        // Logout before deletion
        operation.logout();
        assertNull(operation.getCurrentUser(), "User should be logged out before deletion");

        // Delete user
        assertTrue(operation.deleteUser(username), "User deletion should return true");
        assertFalse(operation.isUsernameTaken(username), "Username should not be taken after deletion");
    }
}