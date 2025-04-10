package com.example.hrsm2.whitebox;

import com.example.hrsm2.controller.LoginController;
import com.example.hrsm2.model.User;
import com.example.hrsm2.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * White box tests for LoginController
 * These tests focus on internal paths, branches, and service interactions
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoginControllerWhiteBoxTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private LoginController loginController;
    
    private User regularUser;
    private User adminUser;
    private User superAdminUser;

    @BeforeEach
    public void setUp() throws Exception {
        // Setup test users
        regularUser = mock(User.class);
        when(regularUser.getUsername()).thenReturn("user");
        when(regularUser.isSuperAdmin()).thenReturn(false);
        
        adminUser = mock(User.class);
        when(adminUser.getUsername()).thenReturn("admin");
        when(adminUser.isSuperAdmin()).thenReturn(false);
        
        superAdminUser = mock(User.class);
        when(superAdminUser.getUsername()).thenReturn("super");
        when(superAdminUser.isSuperAdmin()).thenReturn(true);
        
        // Manually inject mocked userService into controller using reflection
        // This is needed because LoginController creates its own instance in constructor
        Field userServiceField = LoginController.class.getDeclaredField("userService");
        userServiceField.setAccessible(true);
        userServiceField.set(loginController, userService);
    }

    /**
     * Test successful authentication
     * Verifies that the authenticate method correctly delegates to the service
     */
    @Test
    public void testAuthenticateSuccess() {
        // Arrange
        when(userService.authenticate("user", "correctPassword")).thenReturn(regularUser);
        
        // Act
        User result = loginController.authenticate("user", "correctPassword");
        
        // Assert
        assertNotNull(result);
        assertEquals("user", result.getUsername());
        
        // Verify interaction
        verify(userService).authenticate("user", "correctPassword");
    }
    
    /**
     * Test failed authentication
     * Verifies the path where authentication fails
     */
    @Test
    public void testAuthenticateFailure() {
        // Arrange
        when(userService.authenticate("user", "wrongPassword")).thenReturn(null);
        
        // Act
        User result = loginController.authenticate("user", "wrongPassword");
        
        // Assert
        assertNull(result);
        
        // Verify interaction
        verify(userService).authenticate("user", "wrongPassword");
    }
    
    /**
     * Test isSuperAdmin - positive case
     * Tests branch where user is a super admin
     */
    @Test
    public void testIsSuperAdminTrue() {
        // Act
        boolean result = loginController.isSuperAdmin(superAdminUser);
        
        // Assert
        assertTrue(result);
        
        // Verify interaction
        verify(superAdminUser).isSuperAdmin();
    }
    
    /**
     * Test isSuperAdmin - negative case with admin user
     * Tests branch where user is not a super admin
     */
    @Test
    public void testIsSuperAdminFalse() {
        // Act
        boolean result = loginController.isSuperAdmin(adminUser);
        
        // Assert
        assertFalse(result);
        
        // Verify interaction
        verify(adminUser).isSuperAdmin();
    }
    
    /**
     * Test isSuperAdmin with null user
     * Tests branch where null is passed
     */
    @Test
    public void testIsSuperAdminWithNull() {
        // Act
        boolean result = loginController.isSuperAdmin(null);
        
        // Assert
        assertFalse(result);
    }
    
    /**
     * Test getCurrentUser
     * Verifies the delegation to the service
     */
    @Test
    public void testGetCurrentUser() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(regularUser);
        
        // Act
        User result = loginController.getCurrentUser();
        
        // Assert
        assertNotNull(result);
        assertEquals("user", result.getUsername());
        
        // Verify interaction
        verify(userService).getCurrentUser();
    }
    
    /**
     * Test getCurrentUser when no user is logged in
     * Tests boundary condition
     */
    @Test
    public void testGetCurrentUserWhenNoneLoggedIn() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(null);
        
        // Act
        User result = loginController.getCurrentUser();
        
        // Assert
        assertNull(result);
        
        // Verify interaction
        verify(userService).getCurrentUser();
    }
    
    /**
     * Test logout
     * Verifies the delegation to the service
     */
    @Test
    public void testLogout() {
        // Act
        loginController.logout();
        
        // Verify interaction
        verify(userService).logout();
    }
} 