package com.fsm.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Technician entity
 */
class TechnicianTest {
    
    @Test
    @DisplayName("Should create Technician with valid data using factory method")
    void shouldCreateTechnicianWithValidData() {
        // Arrange
        String name = "John Smith";
        String email = "john.smith@example.com";
        String phone = "555-0123";
        TechnicianStatus status = TechnicianStatus.AVAILABLE;
        Location location = Location.createLocation(40.7128, -74.0060, LocalDateTime.now());
        
        // Act
        Technician technician = Technician.createTechnician(name, email, phone, status, location);
        
        // Assert
        assertNotNull(technician);
        assertEquals(name, technician.getName());
        assertEquals(email, technician.getEmail());
        assertEquals(phone, technician.getPhone());
        assertEquals(status, technician.getStatus());
        assertEquals(location, technician.getCurrentLocation());
    }
    
    @Test
    @DisplayName("Should create Technician with builder")
    void shouldCreateTechnicianWithBuilder() {
        // Arrange
        Location location = Location.createLocation(37.7749, -122.4194, LocalDateTime.now());
        
        // Act
        Technician technician = Technician.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .phone("555-0456")
                .status(TechnicianStatus.BUSY)
                .currentLocation(location)
                .build();
        
        // Assert
        assertNotNull(technician);
        assertEquals(1L, technician.getId());
        assertEquals("Jane Doe", technician.getName());
        assertEquals("jane.doe@example.com", technician.getEmail());
        assertEquals("555-0456", technician.getPhone());
        assertEquals(TechnicianStatus.BUSY, technician.getStatus());
        assertEquals(location, technician.getCurrentLocation());
    }
    
    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        null, "email@example.com", "555-0123", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have a name", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "   ", "email@example.com", "555-0123", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have a name", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name is empty")
    void shouldThrowExceptionWhenNameIsEmpty() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "", "email@example.com", "555-0123", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have a name", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowExceptionWhenEmailIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "John Smith", null, "555-0123", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have an email", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when email is blank")
    void shouldThrowExceptionWhenEmailIsBlank() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "John Smith", "   ", "555-0123", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have an email", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when email is empty")
    void shouldThrowExceptionWhenEmailIsEmpty() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "John Smith", "", "555-0123", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have an email", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when phone is null")
    void shouldThrowExceptionWhenPhoneIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "John Smith", "email@example.com", null, TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have a phone number", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when phone is blank")
    void shouldThrowExceptionWhenPhoneIsBlank() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "John Smith", "email@example.com", "   ", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have a phone number", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when phone is empty")
    void shouldThrowExceptionWhenPhoneIsEmpty() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "John Smith", "email@example.com", "", TechnicianStatus.AVAILABLE, null)
        );
        assertEquals("Technician must have a phone number", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when status is null")
    void shouldThrowExceptionWhenStatusIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Technician.createTechnician(
                        "John Smith", "email@example.com", "555-0123", null, null)
        );
        assertEquals("Status must be one of the defined enum values", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should create technician with all status values")
    void shouldCreateTechnicianWithAllStatusValues() {
        // Test all status enum values
        for (TechnicianStatus status : TechnicianStatus.values()) {
            Technician technician = Technician.createTechnician(
                    "John Smith", "email@example.com", "555-0123", status, null);
            assertEquals(status, technician.getStatus());
        }
    }
    
    @Test
    @DisplayName("Should allow null location")
    void shouldAllowNullLocation() {
        // Act
        Technician technician = Technician.createTechnician(
                "John Smith", "email@example.com", "555-0123", TechnicianStatus.AVAILABLE, null);
        
        // Assert
        assertNotNull(technician);
        assertNull(technician.getCurrentLocation());
    }
    
    @Test
    @DisplayName("Should use Lombok generated equals and hashCode")
    void shouldUseLombokGeneratedEqualsAndHashCode() {
        // Arrange
        Location location = Location.createLocation(40.7128, -74.0060, LocalDateTime.now());
        Technician technician1 = Technician.builder()
                .id(1L)
                .name("John Smith")
                .email("john@example.com")
                .phone("555-0123")
                .status(TechnicianStatus.AVAILABLE)
                .currentLocation(location)
                .build();
        
        Technician technician2 = Technician.builder()
                .id(1L)
                .name("John Smith")
                .email("john@example.com")
                .phone("555-0123")
                .status(TechnicianStatus.AVAILABLE)
                .currentLocation(location)
                .build();
        
        // Assert
        assertEquals(technician1, technician2);
        assertEquals(technician1.hashCode(), technician2.hashCode());
    }
    
    @Test
    @DisplayName("Should use Lombok generated toString")
    void shouldUseLombokGeneratedToString() {
        // Arrange
        Technician technician = Technician.createTechnician(
                "John Smith", "john@example.com", "555-0123", TechnicianStatus.AVAILABLE, null);
        
        // Act
        String toString = technician.toString();
        
        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("Technician"));
        assertTrue(toString.contains("John Smith"));
    }
}
