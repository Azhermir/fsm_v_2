package com.fsm.user.service;

import com.fsm.user.domain.Technician;
import com.fsm.user.domain.TechnicianStatus;
import com.fsm.user.repository.TechnicianRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnicianService
 */
@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {
    
    @Mock
    private TechnicianRepository technicianRepository;
    
    @InjectMocks
    private TechnicianService technicianService;
    
    @Test
    @DisplayName("Should return all technicians with hardcoded data")
    void shouldReturnAllTechnicians() {
        // Act
        List<Technician> technicians = technicianService.getAllTechnicians();
        
        // Assert
        assertNotNull(technicians);
        // Only active technicians should be returned (not OFFLINE)
        assertEquals(2, technicians.size());
        
        // Verify first technician
        Technician tech1 = technicians.get(0);
        assertEquals(1L, tech1.getId());
        assertEquals("John Smith", tech1.getName());
        assertEquals("john.smith@example.com", tech1.getEmail());
        assertEquals("555-0123", tech1.getPhone());
        assertEquals(TechnicianStatus.AVAILABLE, tech1.getStatus());
        assertNotNull(tech1.getCurrentLocation());
        
        // Verify second technician
        Technician tech2 = technicians.get(1);
        assertEquals(2L, tech2.getId());
        assertEquals("Jane Doe", tech2.getName());
        assertEquals("jane.doe@example.com", tech2.getEmail());
        assertEquals("555-0456", tech2.getPhone());
        assertEquals(TechnicianStatus.BUSY, tech2.getStatus());
        assertNotNull(tech2.getCurrentLocation());
        
        // Verify no OFFLINE technicians are returned
        assertTrue(technicians.stream().noneMatch(tech -> tech.getStatus() == TechnicianStatus.OFFLINE));
    }
    
    @Test
    @DisplayName("Should return technician by ID when exists")
    void shouldReturnTechnicianByIdWhenExists() {
        // Act
        Optional<Technician> result = technicianService.getTechnicianById(1L);
        
        // Assert
        assertTrue(result.isPresent());
        Technician technician = result.get();
        assertEquals(1L, technician.getId());
        assertEquals("John Smith", technician.getName());
        assertEquals("john.smith@example.com", technician.getEmail());
    }
    
    @Test
    @DisplayName("Should return empty when technician ID does not exist")
    void shouldReturnEmptyWhenTechnicianIdDoesNotExist() {
        // Act
        Optional<Technician> result = technicianService.getTechnicianById(999L);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Should return technician by email when exists")
    void shouldReturnTechnicianByEmailWhenExists() {
        // Act
        Optional<Technician> result = technicianService.getTechnicianByEmail("jane.doe@example.com");
        
        // Assert
        assertTrue(result.isPresent());
        Technician technician = result.get();
        assertEquals(2L, technician.getId());
        assertEquals("Jane Doe", technician.getName());
        assertEquals("jane.doe@example.com", technician.getEmail());
    }
    
    @Test
    @DisplayName("Should return empty when technician email does not exist")
    void shouldReturnEmptyWhenTechnicianEmailDoesNotExist() {
        // Act
        Optional<Technician> result = technicianService.getTechnicianByEmail("nonexistent@example.com");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Should return technicians with valid location data")
    void shouldReturnTechniciansWithValidLocationData() {
        // Act
        List<Technician> technicians = technicianService.getAllTechnicians();
        
        // Assert
        for (Technician technician : technicians) {
            assertNotNull(technician.getCurrentLocation());
            assertNotNull(technician.getCurrentLocation().getLatitude());
            assertNotNull(technician.getCurrentLocation().getLongitude());
            assertNotNull(technician.getCurrentLocation().getTimestamp());
            
            // Verify valid coordinates
            assertTrue(technician.getCurrentLocation().getLatitude() >= -90);
            assertTrue(technician.getCurrentLocation().getLatitude() <= 90);
            assertTrue(technician.getCurrentLocation().getLongitude() >= -180);
            assertTrue(technician.getCurrentLocation().getLongitude() <= 180);
        }
    }
    
    @Test
    @DisplayName("Should update technician location when technician exists")
    void shouldUpdateTechnicianLocationWhenTechnicianExists() {
        // Arrange
        Double newLatitude = 34.0522;
        Double newLongitude = -118.2437;
        
        // Act
        Optional<Technician> result = technicianService.updateTechnicianLocation(1L, newLatitude, newLongitude);
        
        // Assert
        assertTrue(result.isPresent());
        Technician technician = result.get();
        assertEquals(1L, technician.getId());
        assertNotNull(technician.getCurrentLocation());
        assertEquals(newLatitude, technician.getCurrentLocation().getLatitude());
        assertEquals(newLongitude, technician.getCurrentLocation().getLongitude());
        assertNotNull(technician.getCurrentLocation().getTimestamp());
    }
    
    @Test
    @DisplayName("Should return empty when updating location for non-existent technician")
    void shouldReturnEmptyWhenUpdatingLocationForNonExistentTechnician() {
        // Arrange
        Double newLatitude = 34.0522;
        Double newLongitude = -118.2437;
        
        // Act
        Optional<Technician> result = technicianService.updateTechnicianLocation(999L, newLatitude, newLongitude);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Should include timestamp when updating technician location")
    void shouldIncludeTimestampWhenUpdatingTechnicianLocation() {
        // Arrange
        Double newLatitude = 37.7749;
        Double newLongitude = -122.4194;
        
        // Act
        Optional<Technician> result = technicianService.updateTechnicianLocation(2L, newLatitude, newLongitude);
        
        // Assert
        assertTrue(result.isPresent());
        Technician technician = result.get();
        assertNotNull(technician.getCurrentLocation());
        assertNotNull(technician.getCurrentLocation().getTimestamp());
        // Timestamp should be recent (within last minute)
        assertTrue(technician.getCurrentLocation().getTimestamp().isAfter(
                LocalDateTime.now().minusMinutes(1)));
    }
    
    @Test
    @DisplayName("Should validate coordinates when updating location")
    void shouldValidateCoordinatesWhenUpdatingLocation() {
        // Test valid coordinates
        Optional<Technician> result1 = technicianService.updateTechnicianLocation(1L, 40.7128, -74.0060);
        assertTrue(result1.isPresent());
        
        // Test boundary coordinates
        Optional<Technician> result2 = technicianService.updateTechnicianLocation(1L, -90.0, -180.0);
        assertTrue(result2.isPresent());
        
        Optional<Technician> result3 = technicianService.updateTechnicianLocation(1L, 90.0, 180.0);
        assertTrue(result3.isPresent());
    }
}
