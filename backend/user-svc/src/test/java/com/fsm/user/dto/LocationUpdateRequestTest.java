package com.fsm.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocationUpdateRequest DTO
 */
class LocationUpdateRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Should create valid LocationUpdateRequest with valid coordinates")
    void shouldCreateValidLocationUpdateRequest() {
        // Arrange & Act
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();
        
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(40.7128, request.getLatitude());
        assertEquals(-74.0060, request.getLongitude());
    }
    
    @Test
    @DisplayName("Should fail validation when latitude is null")
    void shouldFailValidationWhenLatitudeIsNull() {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(null)
                .longitude(-74.0060)
                .build();
        
        // Act
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Latitude is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when longitude is null")
    void shouldFailValidationWhenLongitudeIsNull() {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(40.7128)
                .longitude(null)
                .build();
        
        // Act
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Longitude is required")));
    }
    
    @Test
    @DisplayName("Should fail validation when latitude is less than -90")
    void shouldFailValidationWhenLatitudeIsTooLow() {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(-91.0)
                .longitude(-74.0060)
                .build();
        
        // Act
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Latitude must be at least -90")));
    }
    
    @Test
    @DisplayName("Should fail validation when latitude is greater than 90")
    void shouldFailValidationWhenLatitudeIsTooHigh() {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(91.0)
                .longitude(-74.0060)
                .build();
        
        // Act
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Latitude must be at most 90")));
    }
    
    @Test
    @DisplayName("Should fail validation when longitude is less than -180")
    void shouldFailValidationWhenLongitudeIsTooLow() {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(40.7128)
                .longitude(-181.0)
                .build();
        
        // Act
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Longitude must be at least -180")));
    }
    
    @Test
    @DisplayName("Should fail validation when longitude is greater than 180")
    void shouldFailValidationWhenLongitudeIsTooHigh() {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(40.7128)
                .longitude(181.0)
                .build();
        
        // Act
        Set<ConstraintViolation<LocationUpdateRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Longitude must be at most 180")));
    }
    
    @Test
    @DisplayName("Should accept boundary latitude values")
    void shouldAcceptBoundaryLatitudeValues() {
        // Test minimum boundary
        LocationUpdateRequest requestMin = LocationUpdateRequest.builder()
                .latitude(-90.0)
                .longitude(0.0)
                .build();
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMin = validator.validate(requestMin);
        assertTrue(violationsMin.isEmpty());
        
        // Test maximum boundary
        LocationUpdateRequest requestMax = LocationUpdateRequest.builder()
                .latitude(90.0)
                .longitude(0.0)
                .build();
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMax = validator.validate(requestMax);
        assertTrue(violationsMax.isEmpty());
    }
    
    @Test
    @DisplayName("Should accept boundary longitude values")
    void shouldAcceptBoundaryLongitudeValues() {
        // Test minimum boundary
        LocationUpdateRequest requestMin = LocationUpdateRequest.builder()
                .latitude(0.0)
                .longitude(-180.0)
                .build();
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMin = validator.validate(requestMin);
        assertTrue(violationsMin.isEmpty());
        
        // Test maximum boundary
        LocationUpdateRequest requestMax = LocationUpdateRequest.builder()
                .latitude(0.0)
                .longitude(180.0)
                .build();
        Set<ConstraintViolation<LocationUpdateRequest>> violationsMax = validator.validate(requestMax);
        assertTrue(violationsMax.isEmpty());
    }
}
