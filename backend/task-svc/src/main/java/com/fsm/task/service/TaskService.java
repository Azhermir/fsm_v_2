package com.fsm.task.service;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.CreateServiceTaskRequest;
import com.fsm.task.dto.ServiceTaskResponse;
import com.fsm.task.repository.IServiceTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for ServiceTask business logic
 * Handles task creation and orchestrates repository operations
 */
@Service
@Slf4j
public class TaskService {
    
    private final IServiceTaskRepository taskRepository;
    
    @Autowired
    public TaskService(IServiceTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Create a new service task
     * 
     * @param request the task creation request
     * @return the created task response
     */
    @Transactional
    public ServiceTaskResponse createTask(CreateServiceTaskRequest request) {
        log.info("Creating new service task: {}", request.getTitle());
        
        // Create domain entity using factory method (ensures domain invariants)
        ServiceTask task = ServiceTask.createServiceTask(
                request.getTitle(),
                request.getDescription(),
                request.getClientAddress(),
                request.getPriority(),
                request.getEstimatedDuration()
        );
        
        // Save to repository
        ServiceTask savedTask = taskRepository.save(task);
        
        log.info("Service task created successfully with ID: {}", savedTask.getId());
        
        // Convert to response DTO
        return convertToResponse(savedTask);
    }
    
    /**
     * Get all tasks ordered by creation date (newest first)
     * 
     * @return list of all tasks
     */
    @Transactional(readOnly = true)
    public List<ServiceTaskResponse> getAllTasks() {
        log.info("Retrieving all service tasks");
        
        List<ServiceTask> tasks = taskRepository.findAllOrderByCreatedAtDesc();
        
        log.info("Found {} service tasks", tasks.size());
        
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all tasks filtered by status, ordered by creation date (newest first)
     * 
     * @param status the status to filter by
     * @return list of tasks with the specified status
     */
    @Transactional(readOnly = true)
    public List<ServiceTaskResponse> getTasksByStatus(TaskStatus status) {
        log.info("Retrieving service tasks with status: {}", status);
        
        List<ServiceTask> tasks = taskRepository.findByStatusOrderByCreatedAtDesc(status);
        
        log.info("Found {} service tasks with status {}", tasks.size(), status);
        
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert ServiceTask entity to ServiceTaskResponse DTO
     * 
     * @param task the task entity
     * @return the task response DTO
     */
    private ServiceTaskResponse convertToResponse(ServiceTask task) {
        return ServiceTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientAddress(task.getClientAddress())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
