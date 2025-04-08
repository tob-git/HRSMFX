package com.example.hrsm2.service;

import com.example.hrsm2.model.*;
import com.example.hrsm2.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService operation;
    private User admin;

    @BeforeEach
    void setUp() {
        operation = UserService.getInstance();
        admin = new User("miky2004", "miky123", "Michael Nagi", User.UserRole.HR_ADMIN);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testGetInstance() {
    }

    @Test
    void testAuthenticate() {
        assertSame(admin, operation.authenticate("miky2004", "miky123"));
    }

    @Test
    void testGetCurrentUser() {
    }

    @Test
    void testLogout() {
    }

    @Test
    void testCreateUser() {
    }

    @Test
    void testUpdateUser() {
    }

    @Test
    void testDeleteUser() {
    }

    @Test
    void testGetAllUsers() {
    }

    @Test
    void testGetUserByUsername() {
    }

    @Test
    void testIsUsernameTaken() {
        assertTrue(operation.isUsernameTaken("super"));         // super exists already
        assertFalse(operation.isUsernameTaken("fadij1234"));    // fadij1234 doesn't exists
        assertTrue(operation.isUsernameTaken("miky2004"));      // miky2004 doesn't exist
    }
}