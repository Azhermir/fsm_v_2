package com.fsm.task.service;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskAssignment;
import com.fsm.task.domain.TaskStatus;
import com.fsm.task.dto.AssignTaskRequest;
import com.fsm.task.dto.CreateServiceTaskRequest;
import com.fsm.task.dto.ServiceTaskResponse;
import com.fsm.task.dto.TaskAssignmentResponse;
import com.fsm.task.dto.UpdateTaskStatusRequest;
import com.fsm.task.event.TaskCompletedEvent;
import com.fsm.task.repository.IServiceTaskRepository;
import com.fsm.task.repository.TaskAssignmentRepository;
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
    private final TaskAssignmentRepository assignmentRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Autowired
    public TaskService(IServiceTaskRepository taskRepository,
                      TaskAssignmentRepository assignmentRepository,
                      ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
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
                request.getEstimatedDuration(),
                request.getCreatedBy()
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
     * Domain invariants enforced:
     * - Status transitions must follow valid workflow: ASSIGNED → IN_PROGRESS → COMPLETED
     * - Cannot skip states
     * - Completed tasks must have workSummary
     * - Timestamps are immutable once set
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
        TaskStatus newStatus = request.getStatus();
        
        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);
        
        // Validate workSummary for COMPLETED status
        if (newStatus == TaskStatus.COMPLETED && (request.getWorkSummary() == null || request.getWorkSummary().isBlank())) {
            throw new IllegalArgumentException("Work summary is required when completing a task");
        }
        
        // Set timestamps based on status change
        LocalDateTime now = LocalDateTime.now();
        
        // Set startedAt when status changes to IN_PROGRESS (immutable once set)
        if (newStatus == TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
            task.setStartedAt(now);
            log.info("Task {} started at {}", taskId, now);
        }
        
        // Set completedAt when status changes to COMPLETED (immutable once set)
        if (newStatus == TaskStatus.COMPLETED && task.getCompletedAt() == null) {
            task.setCompletedAt(now);
            task.setWorkSummary(request.getWorkSummary());
            log.info("Task {} completed at {}", taskId, now);
        }
        
        // Update status
        task.setStatus(newStatus);
        
        // Save the updated task
        ServiceTask updatedTask = taskRepository.save(task);
        
        // Publish TaskCompletedEvent if status changed to COMPLETED
        if (newStatus == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            log.info("Publishing TaskCompletedEvent for task {}", taskId);
            
            TaskCompletedEvent event = TaskCompletedEvent.builder()
                    .taskId(updatedTask.getId())
                    .title(updatedTask.getTitle())
                    .clientAddress(updatedTask.getClientAddress())
                    .workSummary(request.getWorkSummary())
                    .completionTime(updatedTask.getCompletedAt())
                    .build();
            
            eventPublisher.publishEvent(event);
            log.info("TaskCompletedEvent published for task {}", taskId);
        }
        
        log.info("Task {} status updated successfully", taskId);
        return convertToResponse(updatedTask);
    }
    
    /**
     * Validate status transitions according to domain rules
     * Valid transitions:
     * - UNASSIGNED → ASSIGNED (via assignment)
     * - ASSIGNED → IN_PROGRESS
     * - IN_PROGRESS → COMPLETED
     * - Any status can stay the same (idempotent)
     * 
     * @param oldStatus current status
     * @param newStatus desired status
     * @throws IllegalArgumentException if transition is invalid
     */
    private void validateStatusTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        // Allow idempotent updates (same status)
        if (oldStatus == newStatus) {
            return;
        }
        
        // Validate transition based on old status
        switch (oldStatus) {
            case UNASSIGNED:
                // From UNASSIGNED, can only go to ASSIGNED (via assignment endpoint)
                if (newStatus != TaskStatus.ASSIGNED) {
                    throw new IllegalArgumentException(
                            "Cannot transition from UNASSIGNED to " + newStatus + ". Task must be assigned first.");
                }
                break;
                
            case ASSIGNED:
                // From ASSIGNED, can only go to IN_PROGRESS
                if (newStatus != TaskStatus.IN_PROGRESS) {
                    throw new IllegalArgumentException(
                            "Cannot transition from ASSIGNED to " + newStatus + ". Task must be started (IN_PROGRESS) first.");
                }
                break;
                
            case IN_PROGRESS:
                // From IN_PROGRESS, can only go to COMPLETED
                if (newStatus != TaskStatus.COMPLETED) {
                    throw new IllegalArgumentException(
                            "Cannot transition from IN_PROGRESS to " + newStatus + ". Task can only be completed.");
                }
                break;
                
            case COMPLETED:
                // Cannot transition from COMPLETED to any other status
                throw new IllegalArgumentException(
                        "Cannot change status of a completed task. Current status: COMPLETED");
        }
    }
    
    /**
     * Assign a task to a technician
     * Domain invariants:
     * - Task must exist and be in Unassigned or Assigned status
     * - A task can only be assigned to one technician at a time
     * - Technician ID must be provided
     * 
     * @param taskId the task ID
     * @param request the assign task request containing technician ID
     * @return the assignment response
     */
    @Transactional
    public TaskAssignmentResponse assignTask(Long taskId, AssignTaskRequest request) {
        log.info("Assigning task {} to technician {}", taskId, request.getTechnicianId());
        
        // Find the task
        ServiceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        
        // Validate task status - only UNASSIGNED or ASSIGNED tasks can be (re)assigned
        if (task.getStatus() != TaskStatus.UNASSIGNED && task.getStatus() != TaskStatus.ASSIGNED) {
            throw new IllegalArgumentException(
                    "Task can only be assigned when in UNASSIGNED or ASSIGNED status. Current status: " + task.getStatus());
        }
        
        // Set assignedBy to "system" if not provided
        String assignedBy = request.getAssignedBy();
        if (assignedBy == null || assignedBy.isBlank()) {
            assignedBy = "system";
        }
        
        // Create assignment record
        TaskAssignment assignment = TaskAssignment.createAssignment(
                taskId,
                request.getTechnicianId(),
                assignedBy
        );
        
        TaskAssignment savedAssignment = assignmentRepository.save(assignment);
        
        // Update task status to ASSIGNED and set assignedTo
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedTo(request.getTechnicianId());
        taskRepository.save(task);
        
        log.info("Task {} assigned to technician {} successfully", taskId, request.getTechnicianId());
        
        return TaskAssignmentResponse.builder()
                .id(savedAssignment.getId())
                .taskId(savedAssignment.getTaskId())
                .technicianId(savedAssignment.getTechnicianId())
                .assignedAt(savedAssignment.getAssignedAt())
                .assignedBy(savedAssignment.getAssignedBy())
                .build();
    }
    
    /**
     * Reassign a task to a different technician
     * Domain invariants:
     * - Task must exist and be currently assigned
     * - Cannot reassign to the same technician
     * - Reassignment history must be maintained for audit
     * 
     * @param taskId the task ID
     * @param request the reassignment request containing new technician ID and optional reason
     * @return the new assignment response
     */
    @Transactional
    public TaskAssignmentResponse reassignTask(Long taskId, com.fsm.task.dto.ReassignTaskRequest request) {
        log.info("Reassigning task {} to technician {}", taskId, request.getTechnicianId());
        
        // Find the task
        ServiceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        
        // Validate task must be currently assigned to reassign
        if (task.getAssignedTo() == null) {
            throw new IllegalArgumentException("Task must be currently assigned to reassign");
        }
        
        // Validate cannot reassign to the same technician
        if (task.getAssignedTo().equals(request.getTechnicianId())) {
            throw new IllegalArgumentException("Cannot reassign to the same technician");
        }
        
        // Capture old technician ID before updating
        Long oldTechnicianId = task.getAssignedTo();
        
        // Set reassignedBy to "system" if not provided
        String reassignedBy = request.getReassignedBy();
        if (reassignedBy == null || reassignedBy.isBlank()) {
            reassignedBy = "system";
        }
        
        // Create new assignment record with reason (previous assignment remains in history)
        TaskAssignment assignment = TaskAssignment.createAssignment(
                taskId,
                request.getTechnicianId(),
                reassignedBy,
                request.getReason()
        );
        
        TaskAssignment savedAssignment = assignmentRepository.save(assignment);
        
        // Update task's assignedTo field to new technician
        task.setAssignedTo(request.getTechnicianId());
        taskRepository.save(task);
        
        log.info("Task {} reassigned from technician {} to {} successfully", 
                taskId, oldTechnicianId, request.getTechnicianId());
        
        return TaskAssignmentResponse.builder()
                .id(savedAssignment.getId())
                .taskId(savedAssignment.getTaskId())
                .technicianId(savedAssignment.getTechnicianId())
                .assignedAt(savedAssignment.getAssignedAt())
                .assignedBy(savedAssignment.getAssignedBy())
                .build();
    }
    
    /**
     * Get all tasks assigned to a specific technician
     * Optionally filter by status
     * Tasks are sorted by priority (descending) and creation date (ascending)
     * This provides tasks in order of importance and scheduled time for mobile workflow
     * 
     * @param technicianId the technician ID
     * @param status optional status filter
     * @return list of tasks assigned to the technician
     */
    @Transactional(readOnly = true)
    public List<ServiceTaskResponse> getTechnicianTasks(Long technicianId, TaskStatus status) {
        if (status != null) {
            log.info("Retrieving tasks for technician {} with status: {}", technicianId, status);
        } else {
            log.info("Retrieving tasks for technician {}", technicianId);
        }
        
        List<ServiceTask> tasks;
        if (status != null) {
            tasks = taskRepository.findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(technicianId, status);
        } else {
            tasks = taskRepository.findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId);
        }
        
        log.info("Found {} tasks for technician {}", tasks.size(), technicianId);
        
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
                .latitude(task.getLatitude())
                .longitude(task.getLongitude())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .assignedTo(task.getAssignedTo())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .workSummary(task.getWorkSummary())
                .build();
    }
}
