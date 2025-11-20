package com.fsm.taskmanagement.controller;

import com.fsm.taskmanagement.dto.CreateServiceTaskRequest;
import com.fsm.taskmanagement.dto.ServiceTaskResponse;
import com.fsm.taskmanagement.service.ServiceTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Service Tasks", description = "API for managing service tasks")
public class ServiceTaskController {

    private final ServiceTaskService service;

    @PostMapping
    @Operation(summary = "Create a new service task", description = "Creates a new service task with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = ServiceTaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content)
    })
    public ResponseEntity<ServiceTaskResponse> createTask(@Valid @RequestBody CreateServiceTaskRequest request) {
        log.info("Received request to create service task: {}", request.getTitle());

        try {
            ServiceTaskResponse response = service.createTask(request);
            log.info("Successfully created task with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error while creating task: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating service task", e);
            throw e;
        }
    }
}
