package com.fsm.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User aggregate domain model representing a system user
 * This is the root entity of the User aggregate
 * Domain Concepts: User with authentication and authorization
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Email address of the user - must be valid email and unique (domain invariant)
     */
    @NotBlank(message = "User must have an email")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;
    
    /**
     * Password hash - must never be stored in plain text (domain invariant)
     */
    @NotBlank(message = "User must have a password hash")
    @Column(nullable = false)
    private String passwordHash;
    
    /**
     * Name of the user - must not be blank (domain invariant)
     */
    @NotBlank(message = "User must have a name")
    @Column(nullable = false)
    private String name;
    
    /**
     * Role of the user - each user must have exactly one role (domain invariant)
     */
    @NotNull(message = "User must have exactly one role")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    /**
     * Factory method to create a User with required fields and domain invariant validation
     * 
     * @param email the user's email (must not be blank and valid)
     * @param passwordHash the user's password hash (must not be blank)
     * @param name the user's name (must not be blank)
     * @param role the user's role (must be valid enum)
     * @return a new User instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static User createUser(
            String email,
            String passwordHash,
            String name,
            UserRole role) {
        
        // Validate domain invariants
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User must have an email");
        }
        
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("User must have a password hash");
        }
        
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User must have a name");
        }
        
        if (role == null) {
            throw new IllegalArgumentException("User must have exactly one role");
        }
        
        return User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .name(name)
                .role(role)
                .build();
    }
}
