package com.fsm.taskmanagement.service;

import com.fsm.taskmanagement.dto.CreateServiceTaskRequest;
import com.fsm.taskmanagement.dto.ServiceTaskResponse;
import com.fsm.taskmanagement.entity.ServiceTask;
import com.fsm.taskmanagement.entity.Status;
import com.fsm.taskmanagement.repository.ServiceTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceTaskService {

    private final ServiceTaskRepository repository;

    @Transactional
    public ServiceTaskResponse createTask(CreateServiceTaskRequest request) {
        log.info("Creating new service task with title: {}", request.getTitle());

        // Create entity from request
        ServiceTask task = ServiceTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .clientAddress(request.getClientAddress())
                .priority(request.getPriority())
                .estimatedDuration(request.getEstimatedDuration())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(Status.UNASSIGNED) // Default status
                .build();

        // Validate domain invariants
        task.validate();

        // Save to repository
        ServiceTask savedTask = repository.save(task);

        log.info("Successfully created service task with ID: {}", savedTask.getId());

        // Convert to response DTO
        return mapToResponse(savedTask);
    }

    private ServiceTaskResponse mapToResponse(ServiceTask task) {
        return ServiceTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientAddress(task.getClientAddress())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .latitude(task.getLatitude())
                .longitude(task.getLongitude())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
