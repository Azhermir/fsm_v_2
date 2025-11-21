package com.fsm.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.user.domain.Location;
import com.fsm.user.domain.Technician;
import com.fsm.user.domain.TechnicianStatus;
import com.fsm.user.dto.LocationUpdateRequest;
import com.fsm.user.service.TechnicianService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TechnicianController
 */
@WebMvcTest(TechnicianController.class)
@AutoConfigureMockMvc(addFilters = false)
class TechnicianControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TechnicianService technicianService;
    
    @Test
    @DisplayName("Should return all technicians")
    void shouldReturnAllTechnicians() throws Exception {
        // Arrange
        Location location = Location.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .timestamp(LocalDateTime.now())
                .build();
        
        Technician technician = Technician.builder()
                .id(1L)
                .name("John Smith")
                .email("john.smith@example.com")
                .phone("555-0123")
                .status(TechnicianStatus.AVAILABLE)
                .currentLocation(location)
                .build();
        
        when(technicianService.getAllTechnicians()).thenReturn(List.of(technician));
        
        // Act & Assert
        mockMvc.perform(get("/api/technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Smith"))
                .andExpect(jsonPath("$[0].email").value("john.smith@example.com"))
                .andExpect(jsonPath("$[0].phone").value("555-0123"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].currentLocation.latitude").value(40.7128))
                .andExpect(jsonPath("$[0].currentLocation.longitude").value(-74.0060));
    }
    
    @Test
    @DisplayName("Should return technician by ID when exists")
    void shouldReturnTechnicianByIdWhenExists() throws Exception {
        // Arrange
        Location location = Location.builder()
                .latitude(37.7749)
                .longitude(-122.4194)
                .timestamp(LocalDateTime.now())
                .build();
        
        Technician technician = Technician.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .phone("555-0456")
                .status(TechnicianStatus.BUSY)
                .currentLocation(location)
                .build();
        
        when(technicianService.getTechnicianById(1L)).thenReturn(Optional.of(technician));
        
        // Act & Assert
        mockMvc.perform(get("/api/technicians/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("555-0456"))
                .andExpect(jsonPath("$.status").value("BUSY"))
                .andExpect(jsonPath("$.currentLocation.latitude").value(37.7749))
                .andExpect(jsonPath("$.currentLocation.longitude").value(-122.4194));
    }
    
    @Test
    @DisplayName("Should return 404 when technician not found")
    void shouldReturn404WhenTechnicianNotFound() throws Exception {
        // Arrange
        when(technicianService.getTechnicianById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/technicians/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should return empty list when no technicians")
    void shouldReturnEmptyListWhenNoTechnicians() throws Exception {
        // Arrange
        when(technicianService.getAllTechnicians()).thenReturn(List.of());
        
        // Act & Assert
        mockMvc.perform(get("/api/technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    @DisplayName("Should update technician location successfully")
    void shouldUpdateTechnicianLocationSuccessfully() throws Exception {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(34.0522)
                .longitude(-118.2437)
                .build();
        
        Location updatedLocation = Location.builder()
                .latitude(34.0522)
                .longitude(-118.2437)
                .timestamp(LocalDateTime.now())
                .build();
        
        Technician technician = Technician.builder()
                .id(1L)
                .name("John Smith")
                .email("john.smith@example.com")
                .phone("555-0123")
                .status(TechnicianStatus.AVAILABLE)
                .currentLocation(updatedLocation)
                .build();
        
        when(technicianService.updateTechnicianLocation(eq(1L), eq(34.0522), eq(-118.2437)))
                .thenReturn(Optional.of(technician));
        
        // Act & Assert
        mockMvc.perform(put("/api/technicians/1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Smith"))
                .andExpect(jsonPath("$.currentLocation.latitude").value(34.0522))
                .andExpect(jsonPath("$.currentLocation.longitude").value(-118.2437))
                .andExpect(jsonPath("$.currentLocation.timestamp").exists());
    }
    
    @Test
    @DisplayName("Should return 404 when updating location for non-existent technician")
    void shouldReturn404WhenUpdatingLocationForNonExistentTechnician() throws Exception {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(34.0522)
                .longitude(-118.2437)
                .build();
        
        when(technicianService.updateTechnicianLocation(eq(999L), any(Double.class), any(Double.class)))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(put("/api/technicians/999/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should return 400 when latitude is null")
    void shouldReturn400WhenLatitudeIsNull() throws Exception {
        // Arrange
        String requestJson = "{\"longitude\": -118.2437}";
        
        // Act & Assert
        mockMvc.perform(put("/api/technicians/1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 when longitude is null")
    void shouldReturn400WhenLongitudeIsNull() throws Exception {
        // Arrange
        String requestJson = "{\"latitude\": 34.0522}";
        
        // Act & Assert
        mockMvc.perform(put("/api/technicians/1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 when latitude is out of range")
    void shouldReturn400WhenLatitudeIsOutOfRange() throws Exception {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(91.0)
                .longitude(-118.2437)
                .build();
        
        // Act & Assert
        mockMvc.perform(put("/api/technicians/1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 when longitude is out of range")
    void shouldReturn400WhenLongitudeIsOutOfRange() throws Exception {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(34.0522)
                .longitude(181.0)
                .build();
        
        // Act & Assert
        mockMvc.perform(put("/api/technicians/1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should accept boundary coordinate values")
    void shouldAcceptBoundaryCoordinateValues() throws Exception {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(-90.0)
                .longitude(-180.0)
                .build();
        
        Location updatedLocation = Location.builder()
                .latitude(-90.0)
                .longitude(-180.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Technician technician = Technician.builder()
                .id(1L)
                .name("John Smith")
                .email("john.smith@example.com")
                .phone("555-0123")
                .status(TechnicianStatus.AVAILABLE)
                .currentLocation(updatedLocation)
                .build();
        
        when(technicianService.updateTechnicianLocation(eq(1L), eq(-90.0), eq(-180.0)))
                .thenReturn(Optional.of(technician));
        
        // Act & Assert
        mockMvc.perform(put("/api/technicians/1/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLocation.latitude").value(-90.0))
                .andExpect(jsonPath("$.currentLocation.longitude").value(-180.0));
    }
}
