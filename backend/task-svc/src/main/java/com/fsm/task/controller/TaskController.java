package com.fsm.task.controller;

import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.CreateServiceTaskRequest;
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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for ServiceTask operations
 * Provides endpoints for task management
 */
@RestController
@RequestMapping("/api/tasks")
@Slf4j
@Tag(name = "Tasks", description = "Service Task Management API")
public class TaskController {
    
    private final TaskService taskService;
    
    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    /**
     * Create a new service task
     * 
     * @param request the task creation request
     * @return the created task with 201 status
     */
    @PostMapping
    @Operation(summary = "Create a new service task", 
               description = "Creates a new service task with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", 
                     description = "Task created successfully",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ServiceTaskResponse.class))),
        @ApiResponse(responseCode = "400", 
                     description = "Invalid request data",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", 
                     description = "Internal server error",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceTaskResponse> createTask(
            @Valid @RequestBody CreateServiceTaskRequest request) {
        
        log.info("POST /api/tasks - Creating new task: {}", request.getTitle());
        
        try {
            ServiceTaskResponse response = taskService.createTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid task data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating task", e);
            throw e;
        }
    }
    
    /**
     * Get all service tasks
     * 
     * @param status optional status filter
     * @return list of tasks with 200 status
     */
    @GetMapping
    @Operation(summary = "Get all service tasks", 
               description = "Retrieves all service tasks, optionally filtered by status. Tasks are returned in creation order (newest first)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                     description = "Tasks retrieved successfully",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ServiceTaskResponse.class))),
        @ApiResponse(responseCode = "400", 
                     description = "Invalid status parameter",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", 
                     description = "Internal server error",
                     content = @Content(mediaType = "application/json",
                                      schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ServiceTaskResponse>> getAllTasks(
            @Parameter(description = "Optional status filter (UNASSIGNED, ASSIGNED, IN_PROGRESS, COMPLETED)")
            @RequestParam(required = false) TaskStatus status) {
        
        log.info("GET /api/tasks - Retrieving tasks" + (status != null ? " with status: " + status : ""));
        
        try {
            List<ServiceTaskResponse> tasks;
            if (status != null) {
                tasks = taskService.getTasksByStatus(status);
            } else {
                tasks = taskService.getAllTasks();
            }
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving tasks", e);
            throw e;
        }
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Validation failed")
                .validationErrors(validationErrors)
                .path("/api/tasks")
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle illegal argument exceptions (domain validation errors)
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
                .path("/api/tasks")
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
                .path("/api/tasks")
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
