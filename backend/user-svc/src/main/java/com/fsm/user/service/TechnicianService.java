package com.fsm.user.service;

import com.fsm.user.domain.Location;
import com.fsm.user.domain.Technician;
import com.fsm.user.domain.TechnicianStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Technician management
 * Returns hardcoded static technician data for now
 */
@Service
public class TechnicianService {
    
    /**
     * Get all active technicians (AVAILABLE or BUSY, not OFFLINE)
     * Returns hardcoded static data for now
     * Domain Invariant: Only active technicians should be returned
     * 
     * @return list of all active technicians
     */
    public List<Technician> getAllTechnicians() {
        // Return hardcoded static technician data
        Technician tech1 = Technician.builder()
                .id(1L)
                .name("John Smith")
                .email("john.smith@example.com")
                .phone("555-0123")
                .status(TechnicianStatus.AVAILABLE)
                .currentLocation(Location.builder()
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .timestamp(LocalDateTime.now())
                        .build())
                .build();
        
        Technician tech2 = Technician.builder()
                .id(2L)
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .phone("555-0456")
                .status(TechnicianStatus.BUSY)
                .currentLocation(Location.builder()
                        .latitude(37.7749)
                        .longitude(-122.4194)
                        .timestamp(LocalDateTime.now())
                        .build())
                .build();
        
        Technician tech3 = Technician.builder()
                .id(3L)
                .name("Bob Wilson")
                .email("bob.wilson@example.com")
                .phone("555-0789")
                .status(TechnicianStatus.OFFLINE)
                .currentLocation(Location.builder()
                        .latitude(34.0522)
                        .longitude(-118.2437)
                        .timestamp(LocalDateTime.now())
                        .build())
                .build();
        
        // Filter to only return active technicians (not OFFLINE)
        return List.of(tech1, tech2, tech3).stream()
                .filter(tech -> tech.getStatus() != TechnicianStatus.OFFLINE)
                .toList();
    }
    
    /**
     * Get technician by ID
     * Returns hardcoded static data for now
     * 
     * @param id the technician ID
     * @return Optional containing the technician if found
     */
    public Optional<Technician> getTechnicianById(Long id) {
        // Return hardcoded static data
        return getAllTechnicians().stream()
                .filter(tech -> tech.getId().equals(id))
                .findFirst();
    }
    
    /**
     * Get technician by email
     * Returns hardcoded static data for now
     * 
     * @param email the technician email
     * @return Optional containing the technician if found
     */
    public Optional<Technician> getTechnicianByEmail(String email) {
        // Return hardcoded static data
        return getAllTechnicians().stream()
                .filter(tech -> tech.getEmail().equals(email))
                .findFirst();
    }
    
    /**
     * Update technician location
     * Updates the location with current timestamp
     * Domain Invariant: Location updates should include timestamp
     * 
     * @param id the technician ID
     * @param latitude the new latitude
     * @param longitude the new longitude
     * @return Optional containing the updated technician if found
     */
    public Optional<Technician> updateTechnicianLocation(Long id, Double latitude, Double longitude) {
        return getTechnicianById(id).map(technician -> {
            // Create new location with current timestamp
            Location newLocation = Location.createLocation(latitude, longitude, LocalDateTime.now());
            technician.setCurrentLocation(newLocation);
            return technician;
        });
    }
}
