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
 * Technician aggregate domain model representing a field service technician
 * This is the root entity of the Technician aggregate
 * Domain Concepts: Technician with location tracking capability
 */
@Entity
@Table(name = "technicians")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Technician {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Name of the technician - must not be blank (domain invariant)
     */
    @NotBlank(message = "Technician must have a name")
    @Column(nullable = false)
    private String name;
    
    /**
     * Email address of the technician - must be valid email (domain invariant)
     */
    @NotBlank(message = "Technician must have an email")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;
    
    /**
     * Phone number of the technician - must not be blank (domain invariant)
     */
    @NotBlank(message = "Technician must have a phone number")
    @Column(nullable = false)
    private String phone;
    
    /**
     * Current status of the technician - must be one of the defined enum values (domain invariant)
     */
    @NotNull(message = "Status must be one of the defined enum values")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechnicianStatus status;
    
    /**
     * Current location of the technician with valid coordinates (domain invariant)
     */
    @Embedded
    private Location currentLocation;
    
    /**
     * Factory method to create a Technician with required fields and domain invariant validation
     * 
     * @param name the technician's name (must not be blank)
     * @param email the technician's email (must not be blank and valid)
     * @param phone the technician's phone (must not be blank)
     * @param status the technician's status (must be valid enum)
     * @param currentLocation the technician's current location
     * @return a new Technician instance
     * @throws IllegalArgumentException if domain invariants are violated
     */
    public static Technician createTechnician(
            String name,
            String email,
            String phone,
            TechnicianStatus status,
            Location currentLocation) {
        
        // Validate domain invariants
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Technician must have a name");
        }
        
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Technician must have an email");
        }
        
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Technician must have a phone number");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("Status must be one of the defined enum values");
        }
        
        return Technician.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .status(status)
                .currentLocation(currentLocation)
                .build();
    }
}
