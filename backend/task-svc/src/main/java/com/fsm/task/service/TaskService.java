package com.fsm.task.service;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.CreateServiceTaskRequest;
import com.fsm.task.dto.ServiceTaskResponse;
import com.fsm.task.dto.UpdateTaskStatusRequest;
import com.fsm.task.event.TaskCompletedEvent;
import com.fsm.task.repository.IServiceTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ApplicationEventPublisher eventPublisher;
    
    @Autowired
    public TaskService(IServiceTaskRepository taskRepository, 
                      ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
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
                request.getLatitude(),
                request.getLongitude(),
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
     * Update task status
     * Publishes TaskCompletedEvent when status changes to COMPLETED
     * 
     * @param taskId the task ID
     * @param request the update status request
     * @return the updated task response
     */
    @Transactional
    public ServiceTaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request) {
        log.info("Updating task {} status to {}", taskId, request.getStatus());
        
        // Find the task
        ServiceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(request.getStatus());
        
        // Save the updated task
        ServiceTask updatedTask = taskRepository.save(task);
        
        // Publish TaskCompletedEvent if status changed to COMPLETED
        if (request.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            log.info("Publishing TaskCompletedEvent for task {}", taskId);
            
            TaskCompletedEvent event = TaskCompletedEvent.builder()
                    .taskId(updatedTask.getId())
                    .title(updatedTask.getTitle())
                    .clientAddress(updatedTask.getClientAddress())
                    .workSummary(request.getWorkSummary())
                    .completionTime(LocalDateTime.now())
                    .build();
            
            eventPublisher.publishEvent(event);
            log.info("TaskCompletedEvent published for task {}", taskId);
        }
        
        log.info("Task {} status updated successfully", taskId);
        return convertToResponse(updatedTask);
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
                .latitude(task.getLatitude())
                .longitude(task.getLongitude())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
