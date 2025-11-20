package com.fsm.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Location value object
 */
class LocationTest {
    
    @Test
    @DisplayName("Should create Location with valid coordinates using factory method")
    void shouldCreateLocationWithValidCoordinates() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        LocalDateTime timestamp = LocalDateTime.now();
        
        // Act
        Location location = Location.createLocation(latitude, longitude, timestamp);
        
        // Assert
        assertNotNull(location);
        assertEquals(latitude, location.getLatitude());
        assertEquals(longitude, location.getLongitude());
        assertEquals(timestamp, location.getTimestamp());
    }
    
    @Test
    @DisplayName("Should create Location with builder")
    void shouldCreateLocationWithBuilder() {
        // Arrange
        Double latitude = 37.7749;
        Double longitude = -122.4194;
        LocalDateTime timestamp = LocalDateTime.now();
        
        // Act
        Location location = Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .timestamp(timestamp)
                .build();
        
        // Assert
        assertNotNull(location);
        assertEquals(latitude, location.getLatitude());
        assertEquals(longitude, location.getLongitude());
        assertEquals(timestamp, location.getTimestamp());
    }
    
    @Test
    @DisplayName("Should throw exception when latitude is null")
    void shouldThrowExceptionWhenLatitudeIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.createLocation(null, -74.0060, LocalDateTime.now())
        );
        assertEquals("Latitude must not be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when longitude is null")
    void shouldThrowExceptionWhenLongitudeIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.createLocation(40.7128, null, LocalDateTime.now())
        );
        assertEquals("Longitude must not be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when timestamp is null")
    void shouldThrowExceptionWhenTimestampIsNull() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.createLocation(40.7128, -74.0060, null)
        );
        assertEquals("Timestamp must not be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when latitude is less than -90")
    void shouldThrowExceptionWhenLatitudeIsLessThanMinimum() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.createLocation(-91.0, -74.0060, LocalDateTime.now())
        );
        assertEquals("Latitude must be between -90 and 90", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when latitude is greater than 90")
    void shouldThrowExceptionWhenLatitudeIsGreaterThanMaximum() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.createLocation(91.0, -74.0060, LocalDateTime.now())
        );
        assertEquals("Latitude must be between -90 and 90", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when longitude is less than -180")
    void shouldThrowExceptionWhenLongitudeIsLessThanMinimum() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.createLocation(40.7128, -181.0, LocalDateTime.now())
        );
        assertEquals("Longitude must be between -180 and 180", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when longitude is greater than 180")
    void shouldThrowExceptionWhenLongitudeIsGreaterThanMaximum() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Location.createLocation(40.7128, 181.0, LocalDateTime.now())
        );
        assertEquals("Longitude must be between -180 and 180", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should create Location with boundary latitude values")
    void shouldCreateLocationWithBoundaryLatitudeValues() {
        // Test minimum latitude
        Location minLatLocation = Location.createLocation(-90.0, 0.0, LocalDateTime.now());
        assertEquals(-90.0, minLatLocation.getLatitude());
        
        // Test maximum latitude
        Location maxLatLocation = Location.createLocation(90.0, 0.0, LocalDateTime.now());
        assertEquals(90.0, maxLatLocation.getLatitude());
    }
    
    @Test
    @DisplayName("Should create Location with boundary longitude values")
    void shouldCreateLocationWithBoundaryLongitudeValues() {
        // Test minimum longitude
        Location minLonLocation = Location.createLocation(0.0, -180.0, LocalDateTime.now());
        assertEquals(-180.0, minLonLocation.getLongitude());
        
        // Test maximum longitude
        Location maxLonLocation = Location.createLocation(0.0, 180.0, LocalDateTime.now());
        assertEquals(180.0, maxLonLocation.getLongitude());
    }
    
    @Test
    @DisplayName("Should use Lombok generated equals and hashCode")
    void shouldUseLombokGeneratedEqualsAndHashCode() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        Location location1 = Location.createLocation(40.7128, -74.0060, timestamp);
        Location location2 = Location.createLocation(40.7128, -74.0060, timestamp);
        
        // Assert
        assertEquals(location1, location2);
        assertEquals(location1.hashCode(), location2.hashCode());
    }
    
    @Test
    @DisplayName("Should use Lombok generated toString")
    void shouldUseLombokGeneratedToString() {
        // Arrange
        Location location = Location.createLocation(40.7128, -74.0060, LocalDateTime.now());
        
        // Act
        String toString = location.toString();
        
        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("Location"));
    }
}
