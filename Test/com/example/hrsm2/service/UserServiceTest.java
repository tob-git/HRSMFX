package com.example.hrsm2.service;

import com.example.hrsm2.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService operation;

    @BeforeEach
    void setUp() {
        operation = UserService.getInstance();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getInstance() {
    }

    @Test
    void authenticate() {
    }

    @Test
    void getCurrentUser() {
    }

    @Test
    void logout() {
    }

    @Test
    void createUser() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void getAllUsers() {
    }

    @Test
    void getUserByUsername() {
    }

    @Test
    void testIsUsernameTaken() {
        assertTrue(operation.isUsernameTaken("fadij"));        // fadij exists already
        assertFalse(operation.isUsernameTaken("fadij1234"));   // fadij1234 doesn't exists
        assertFalse(operation.isUsernameTaken("miky2004"));    // miky2004 doesn't exist
    }
}