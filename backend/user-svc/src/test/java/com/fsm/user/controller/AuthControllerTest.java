package com.fsm.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.user.domain.UserRole;
import com.fsm.user.dto.AuthResponse;
import com.fsm.user.dto.LoginRequest;
import com.fsm.user.dto.RegisterRequest;
import com.fsm.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        AuthResponse response = AuthResponse.builder()
                .token("test-token")
                .email(request.getEmail())
                .name(request.getName())
                .role(request.getRole().name())
                .build();
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.role").value(request.getRole().name()));
    }
    
    @Test
    @DisplayName("Should return bad request when registering with existing email")
    void shouldReturnBadRequestWhenRegisteringWithExistingEmail() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("User with this email already exists"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should login user successfully")
    void shouldLoginUserSuccessfully() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
        
        AuthResponse response = AuthResponse.builder()
                .token("test-token")
                .email(request.getEmail())
                .name("Test User")
                .role("DISPATCHER")
                .build();
        
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("DISPATCHER"));
    }
    
    @Test
    @DisplayName("Should return unauthorized when login with invalid credentials")
    void shouldReturnUnauthorizedWhenLoginWithInvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should return bad request when registering with invalid email")
    void shouldReturnBadRequestWhenRegisteringWithInvalidEmail() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("password123")
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return bad request when registering without password")
    void shouldReturnBadRequestWhenRegisteringWithoutPassword() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("")
                .name("Test User")
                .role(UserRole.DISPATCHER)
                .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
