package com.fsm.task.controller;

import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.ErrorResponse;
import com.fsm.task.dto.ServiceTaskResponse;
import com.fsm.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Technician-specific operations
 * Provides endpoints for mobile technician workflow
 */
@RestController
@RequestMapping("/api/technicians")
@Slf4j
@Tag(name = "Technicians", description = "Technician Task Management API for Mobile")
public class TechnicianController {
    
    private final TaskService taskService;
    
    @Autowired
    public TechnicianController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    /**
     * Get all tasks assigned to a specific technician
     * 
     * @param technicianId the technician ID
     * @param status optional status filter (ASSIGNED, IN_PROGRESS, COMPLETED)
     * @return list of tasks assigned to the technician with 200 status
     */
    @GetMapping("/{technicianId}/tasks")
    @Operation(summary = "Get technician's assigned tasks", 
               description = "Retrieves all tasks assigned to a specific technician. Optionally filter by status. Tasks are sorted by priority (highest first) and creation date (oldest first) for optimal mobile workflow.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                     description = "Tasks retrieved successfully",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ServiceTaskResponse.class))),
        @ApiResponse(responseCode = "400", 
                     description = "Invalid request parameters",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", 
                     description = "Internal server error",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ServiceTaskResponse>> getTechnicianTasks(
            @Parameter(description = "Technician ID")
            @PathVariable Long technicianId,
            @Parameter(description = "Optional status filter (ASSIGNED, IN_PROGRESS, COMPLETED)")
            @RequestParam(required = false) TaskStatus status) {
        
        log.info("GET /api/technicians/{}/tasks - Retrieving tasks" + 
                (status != null ? " with status: " + status : ""), technicianId);
        
        try {
            List<ServiceTaskResponse> tasks = taskService.getTechnicianTasks(technicianId, status);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving technician tasks", e);
            throw e;
        }
    }
    
    /**
     * Handle illegal argument exceptions (validation errors)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path("/api/technicians")
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex) {
        
        log.error("Unexpected error", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path("/api/technicians")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
