package com.fsm.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set secret and expiration using reflection
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }
    
    @Test
    @DisplayName("Should generate token successfully")
    void shouldGenerateTokenSuccessfully() {
        // Arrange
        String username = "test@example.com";
        String role = "DISPATCHER";
        
        // Act
        String token = jwtUtil.generateToken(username, role);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Arrange
        String username = "test@example.com";
        String role = "DISPATCHER";
        String token = jwtUtil.generateToken(username, role);
        
        // Act
        String extractedUsername = jwtUtil.extractUsername(token);
        
        // Assert
        assertEquals(username, extractedUsername);
    }
    
    @Test
    @DisplayName("Should extract expiration from token")
    void shouldExtractExpirationFromToken() {
        // Arrange
        String username = "test@example.com";
        String role = "DISPATCHER";
        String token = jwtUtil.generateToken(username, role);
        
        // Act
        Date expiration = jwtUtil.extractExpiration(token);
        
        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        // Arrange
        String username = "test@example.com";
        String role = "DISPATCHER";
        String token = jwtUtil.generateToken(username, role);
        
        // Act
        Boolean isValid = jwtUtil.validateToken(token, username);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should invalidate token with wrong username")
    void shouldInvalidateTokenWithWrongUsername() {
        // Arrange
        String username = "test@example.com";
        String wrongUsername = "wrong@example.com";
        String role = "DISPATCHER";
        String token = jwtUtil.generateToken(username, role);
        
        // Act
        Boolean isValid = jwtUtil.validateToken(token, wrongUsername);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Arrange
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";
        String role = "DISPATCHER";
        
        // Act
        String token1 = jwtUtil.generateToken(user1, role);
        String token2 = jwtUtil.generateToken(user2, role);
        
        // Assert
        assertNotEquals(token1, token2);
    }
    
    @Test
    @DisplayName("Should include role in token")
    void shouldIncludeRoleInToken() {
        // Arrange
        String username = "test@example.com";
        String role = "DISPATCHER";
        String token = jwtUtil.generateToken(username, role);
        
        // Act - Extract role claim
        String extractedRole = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
        
        // Assert
        assertEquals(role, extractedRole);
    }
}
