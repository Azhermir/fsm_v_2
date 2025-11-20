package com.fsm.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for API error responses
 * Provides consistent error format across all endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response details")
public class ErrorResponse {
    
    @Schema(description = "Timestamp when the error occurred", 
            example = "2025-11-20T22:00:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Error message", example = "Invalid request")
    private String error;
    
    @Schema(description = "Detailed error message", example = "Title is required")
    private String message;
    
    @Schema(description = "List of validation errors (if applicable)")
    private List<String> validationErrors;
    
    @Schema(description = "Request path that caused the error", example = "/api/tasks")
    private String path;
}
