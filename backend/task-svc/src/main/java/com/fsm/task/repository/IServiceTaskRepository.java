package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ServiceTask persistence operations
 * Defines the contract for CRUD operations on ServiceTask aggregates
 */
public interface IServiceTaskRepository {
    
    /**
     * Save or update a ServiceTask
     * Operations should be idempotent
     * 
     * @param serviceTask the ServiceTask to save
     * @return the saved ServiceTask
     */
    ServiceTask save(ServiceTask serviceTask);
    
    /**
     * Find a ServiceTask by its ID
     * 
     * @param id the ID of the ServiceTask
     * @return Optional containing the ServiceTask if found, empty otherwise
     */
    Optional<ServiceTask> findById(Long id);
    
    /**
     * Find all ServiceTasks
     * 
     * @return List of all ServiceTasks
     */
    List<ServiceTask> findAll();
    
    /**
     * Delete a ServiceTask by its ID
     * 
     * @param id the ID of the ServiceTask to delete
     * @return true if the ServiceTask was deleted, false if it didn't exist
     */
    boolean delete(Long id);
    
    /**
     * Find all ServiceTasks ordered by creation date descending (newest first)
     * 
     * @return List of all ServiceTasks in creation order
     */
    List<ServiceTask> findAllOrderByCreatedAtDesc();
    
    /**
     * Find all ServiceTasks by status, ordered by creation date descending (newest first)
     * 
     * @param status the task status to filter by
     * @return List of ServiceTasks with the specified status
     */
    List<ServiceTask> findByStatusOrderByCreatedAtDesc(TaskStatus status);
    
    /**
     * Find all tasks assigned to a specific technician
     * Sorted by priority (descending - HIGH to LOW) and creation date (ascending - oldest first)
     * 
     * @param technicianId the technician ID
     * @return List of ServiceTasks assigned to the technician
     */
    List<ServiceTask> findByAssignedToOrderByPriorityDescCreatedAtAsc(Long technicianId);
    
    /**
     * Find all tasks assigned to a specific technician with a specific status
     * Sorted by priority (descending - HIGH to LOW) and creation date (ascending - oldest first)
     * 
     * @param technicianId the technician ID
     * @param status the task status to filter by
     * @return List of ServiceTasks assigned to the technician with the specified status
     */
    List<ServiceTask> findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(Long technicianId, TaskStatus status);
}
