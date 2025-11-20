package com.fsm.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 */
class UserTest {
    
    @Test
    @DisplayName("Should create User with valid data using factory method")
    void shouldCreateUserWithValidData() {
        // Arrange
        String email = "user@example.com";
        String passwordHash = "$2a$10$hashedPassword";
        String name = "John Doe";
        UserRole role = UserRole.DISPATCHER;
        
        // Act
        User user = User.createUser(email, passwordHash, name, role);
        
        // Assert
        assertNotNull(user);
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(name, user.getName());
        assertEquals(role, user.getRole());
    }
    
    @Test
    @DisplayName("Should create User with builder")
    void shouldCreateUserWithBuilder() {
        // Act
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .name("Test User")
                .role(UserRole.TECHNICIAN)
                .build();
        
        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals("Test User", user.getName());
        assertEquals(UserRole.TECHNICIAN, user.getRole());
    }
    
    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowExceptionWhenEmailIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser(null, "hash", "John", UserRole.DISPATCHER)
        );
        assertEquals("User must have an email", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when email is blank")
    void shouldThrowExceptionWhenEmailIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("   ", "hash", "John", UserRole.DISPATCHER)
        );
        assertEquals("User must have an email", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when email is empty")
    void shouldThrowExceptionWhenEmailIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("", "hash", "John", UserRole.DISPATCHER)
        );
        assertEquals("User must have an email", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when passwordHash is null")
    void shouldThrowExceptionWhenPasswordHashIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("user@example.com", null, "John", UserRole.DISPATCHER)
        );
        assertEquals("User must have a password hash", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when passwordHash is blank")
    void shouldThrowExceptionWhenPasswordHashIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("user@example.com", "   ", "John", UserRole.DISPATCHER)
        );
        assertEquals("User must have a password hash", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when passwordHash is empty")
    void shouldThrowExceptionWhenPasswordHashIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("user@example.com", "", "John", UserRole.DISPATCHER)
        );
        assertEquals("User must have a password hash", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("user@example.com", "hash", null, UserRole.DISPATCHER)
        );
        assertEquals("User must have a name", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("user@example.com", "hash", "   ", UserRole.DISPATCHER)
        );
        assertEquals("User must have a name", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name is empty")
    void shouldThrowExceptionWhenNameIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("user@example.com", "hash", "", UserRole.DISPATCHER)
        );
        assertEquals("User must have a name", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when role is null")
    void shouldThrowExceptionWhenRoleIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> User.createUser("user@example.com", "hash", "John", null)
        );
        assertEquals("User must have exactly one role", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should create user with all role values")
    void shouldCreateUserWithAllRoleValues() {
        // Test all role enum values
        for (UserRole role : UserRole.values()) {
            User user = User.createUser(
                    "user@example.com", "hash", "John Doe", role);
            assertEquals(role, user.getRole());
        }
    }
    
    @Test
    @DisplayName("Should use Lombok generated equals and hashCode")
    void shouldUseLombokGeneratedEqualsAndHashCode() {
        User user1 = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("hash")
                .name("John")
                .role(UserRole.DISPATCHER)
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("hash")
                .name("John")
                .role(UserRole.DISPATCHER)
                .build();
        
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
    
    @Test
    @DisplayName("Should use Lombok generated toString")
    void shouldUseLombokGeneratedToString() {
        User user = User.createUser(
                "user@example.com", "hash", "John Doe", UserRole.DISPATCHER);
        
        String toString = user.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("User"));
    }
}
