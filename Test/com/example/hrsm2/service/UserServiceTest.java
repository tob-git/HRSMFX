package com.example.hrsm2.service;

import com.example.hrsm2.model.*;
import com.example.hrsm2.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class UserServiceTest {

    private UserService operation;
    private User testUser;

    @BeforeEach
    void setUp() {
        operation = UserService.getInstance();
        testUser = new User("testuser", "test123", "Test User", User.UserRole.HR_ADMIN);
        // Clean-up in case the test user already exists
        operation.deleteUser(testUser.getUsername());

    }

    @AfterEach
    void tearDown() {
        operation.deleteUser(testUser.getUsername());
        operation.logout();
    }

    @Test
    void testGetInstance() {
        UserService anotherInstance = UserService.getInstance();
        assertSame(operation, anotherInstance);     // Should be the same singleton instance
    }

    @Test
    void testAuthenticate() {
        operation.createUser(testUser.getUsername(), testUser.getPassword(), testUser.getFullName(), testUser.getRole());
        User authenticated = operation.authenticate("testuser", "test123");
        assertNotNull(authenticated);
        assertEquals("testuser", authenticated.getUsername());

        /*User expectedUser1 = new User("miky2004", "miky123", "Michael Nagi", User.UserRole.HR_ADMIN);
        assertEquals(expectedUser1.toString(), operation.authenticate("miky2004", "miky123").toString());

        User expectedUser2 = new User("super", "super123", "Super Administrator", User.UserRole.HR_ADMIN);
        assertEquals(expectedUser2.toString(),operation.authenticate("super", "super123").toString());

        assertNotEquals(expectedUser1.toString(),operation.authenticate("super", "super123").toString());*/
    }

    @Test
    void testGetCurrentUser() {
        operation.createUser(testUser.getUsername(), testUser.getPassword(), testUser.getFullName(), testUser.getRole());
        operation.authenticate("testuser", "test123");
        User current = operation.getCurrentUser();
        assertNotNull(current);
        assertEquals("testuser", current.getUsername());
    }

    @ParameterizedTest
    @Order(1)
    @CsvSource({
            "fadij, fadij123, Fady John",
            "miky2004, miky123, Michael Nagi"
    })
    void testLogout(String username, String password, String Fname) {
        operation.createUser(username, password, Fname,User.UserRole.HR_ADMIN);

        //operation.createUser(testUser.getUsername(), testUser.getPassword(), testUser.getFullName(), testUser.getRole());
        operation.authenticate(username, password);
        operation.logout();
        // assertNull(operation.getCurrentUser());
    }

    @Test
    void testCreateUser() {
        boolean created = operation.createUser("testuser", "test123", "Test User", User.UserRole.HR_ADMIN);
        assertTrue(created);
        assertTrue(operation.isUsernameTaken("testuser"));
    }

    @Test
    void testUpdateUser() {
        operation.createUser("testuser", "test123", "Test User", User.UserRole.HR_ADMIN);
        User existing = operation.getUserByUsername("testuser");
        User updatedUser = new User(existing.getUsername(), existing.getPassword(), "Updated Name", User.UserRole.SUPER_ADMIN);
        boolean updated = operation.updateUser(updatedUser);
        assertTrue(updated);

        User fromDb = operation.getUserByUsername("testuser");
        assertEquals("Updated Name", fromDb.getFullName());
        assertEquals(User.UserRole.SUPER_ADMIN, fromDb.getRole());
    }

    @Test
    void testDeleteUser() {
        operation.createUser("testuser", "test123", "Test User", User.UserRole.HR_ADMIN);
        assertTrue(operation.deleteUser("testuser"));
        assertFalse(operation.isUsernameTaken("testuser"));
    }

    @Test
    void testGetAllUsers() {
        List<User> users = operation.getAllUsers();
        assertNotNull(users);
        assertTrue(users.size() > 0); // Should contain at least super admin

    }

    @Test
    void testGetUserByUsername() {
        operation.createUser("testuser", "test123", "Test User", User.UserRole.HR_ADMIN);
        User user = operation.getUserByUsername("testuser");
        assertNotNull(user);
        assertEquals("Test User", user.getFullName());
    }

    @Test
    void testIsUsernameTaken() {
        assertTrue(operation.isUsernameTaken("super"));         // super already exists
        assertFalse(operation.isUsernameTaken("fadij1234"));    // fadij1234 doesn't exists
        assertTrue(operation.isUsernameTaken("miky2004"));      // miky2004 already exist
    }
}