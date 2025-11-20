package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;

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
}
