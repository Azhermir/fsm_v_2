package com.fsm.user.service;

import com.fsm.user.domain.User;
import com.fsm.user.domain.UserRole;
import com.fsm.user.dto.AuthResponse;
import com.fsm.user.dto.LoginRequest;
import com.fsm.user.dto.RegisterRequest;
import com.fsm.user.repository.UserRepository;
import com.fsm.user.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthService authService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        User savedUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .passwordHash("hashedPassword")
                .name(request.getName())
                .role(request.getRole())
                .build();
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-token");
        
        // Act
        AuthResponse response = authService.register(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getRole().name(), response.getRole());
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(request.getEmail(), request.getRole().name());
    }
    
    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void shouldThrowExceptionWhenRegisteringWithExistingEmail() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );
        assertEquals("User with this email already exists", exception.getMessage());
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should login user successfully")
    void shouldLoginUserSuccessfully() {
        // Arrange
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password(rawPassword)
                .build();
        
        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-token");
        
        // Act
        AuthResponse response = authService.login(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getRole().name(), response.getRole());
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(jwtUtil).generateToken(user.getEmail(), user.getRole().name());
    }
    
    @Test
    @DisplayName("Should throw exception when login with non-existent email")
    void shouldThrowExceptionWhenLoginWithNonExistentEmail() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should throw exception when login with wrong password")
    void shouldThrowExceptionWhenLoginWithWrongPassword() {
        // Arrange
        String hashedPassword = passwordEncoder.encode("correctPassword");
        
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();
        
        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
        
        verify(userRepository).findByEmail(request.getEmail());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }
}
