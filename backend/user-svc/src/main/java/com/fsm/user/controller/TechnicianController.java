package com.fsm.user.controller;

import com.fsm.user.domain.Technician;
import com.fsm.user.dto.TechnicianResponse;
import com.fsm.user.service.TechnicianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Technician management
 */
@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Tag(name = "Technician Management", description = "APIs for managing field service technicians")
public class TechnicianController {
    
    private final TechnicianService technicianService;
    
    /**
     * Get all technicians
     * 
     * @return list of all technicians
     */
    @GetMapping
    @Operation(summary = "Get all technicians", description = "Retrieve all technicians with their current status and location")
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians() {
        List<TechnicianResponse> responses = technicianService.getAllTechnicians()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get technician by ID
     * 
     * @param id the technician ID
     * @return the technician if found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get technician by ID", description = "Retrieve a specific technician by their ID")
    public ResponseEntity<TechnicianResponse> getTechnicianById(@PathVariable Long id) {
        return technicianService.getTechnicianById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Convert Technician entity to TechnicianResponse DTO
     * 
     * @param technician the technician entity
     * @return the response DTO
     */
    private TechnicianResponse toResponse(Technician technician) {
        return TechnicianResponse.builder()
                .id(technician.getId())
                .name(technician.getName())
                .email(technician.getEmail())
                .phone(technician.getPhone())
                .status(technician.getStatus())
                .currentLocation(technician.getCurrentLocation())
                .build();
    }
}
