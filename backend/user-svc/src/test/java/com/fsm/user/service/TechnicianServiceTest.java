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
        assertEquals(3, technicians.size());
        
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
        
        // Verify third technician
        Technician tech3 = technicians.get(2);
        assertEquals(3L, tech3.getId());
        assertEquals("Bob Wilson", tech3.getName());
        assertEquals("bob.wilson@example.com", tech3.getEmail());
        assertEquals("555-0789", tech3.getPhone());
        assertEquals(TechnicianStatus.OFFLINE, tech3.getStatus());
        assertNotNull(tech3.getCurrentLocation());
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
}
