package com.fsm.user.service;

import com.fsm.user.domain.User;
import com.fsm.user.dto.AuthResponse;
import com.fsm.user.dto.LoginRequest;
import com.fsm.user.dto.RegisterRequest;
import com.fsm.user.repository.UserRepository;
import com.fsm.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer for authentication and user management
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Register a new user
     * Passwords are hashed before storing (domain invariant)
     * 
     * @param request the registration request
     * @return authentication response with JWT token
     * @throws IllegalArgumentException if email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        
        // Hash password (domain invariant - never store plain text)
        String passwordHash = passwordEncoder.encode(request.getPassword());
        
        // Create user
        User user = User.createUser(
                request.getEmail(),
                passwordHash,
                request.getName(),
                request.getRole()
        );
        
        // Save user
        user = userRepository.save(user);
        
        // Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
    
    /**
     * Login a user
     * 
     * @param request the login request
     * @return authentication response with JWT token
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
