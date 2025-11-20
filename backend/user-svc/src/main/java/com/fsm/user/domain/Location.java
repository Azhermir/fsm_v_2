package com.fsm.user.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Location value object representing geographic coordinates with timestamp
 * Domain Concepts: Value object for location tracking
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    
    /**
     * Latitude coordinate - must be between -90 and 90 (domain invariant)
     */
    @NotNull(message = "Latitude must not be null")
    @Min(value = -90, message = "Latitude must be at least -90")
    @Max(value = 90, message = "Latitude must be at most 90")
    private Double latitude;
    
    /**
     * Longitude coordinate - must be between -180 and 180 (domain invariant)
     */
    @NotNull(message = "Longitude must not be null")
    @Min(value = -180, message = "Longitude must be at least -180")
    @Max(value = 180, message = "Longitude must be at most 180")
    private Double longitude;
    
    /**
     * Timestamp when the location was recorded
     */
    @NotNull(message = "Timestamp must not be null")
    private LocalDateTime timestamp;
    
    /**
     * Factory method to create a Location with validation
     * 
     * @param latitude the latitude coordinate
     * @param longitude the longitude coordinate
     * @param timestamp the timestamp when location was recorded
     * @return a new Location instance
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public static Location createLocation(Double latitude, Double longitude, LocalDateTime timestamp) {
        // Validate domain invariants
        if (latitude == null) {
            throw new IllegalArgumentException("Latitude must not be null");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        
        if (longitude == null) {
            throw new IllegalArgumentException("Longitude must not be null");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp must not be null");
        }
        
        return Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .timestamp(timestamp)
                .build();
    }
}
