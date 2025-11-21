package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for ServiceTask entity
 * Provides database persistence operations using PostgreSQL/H2
 */
@Repository
public interface ServiceTaskJpaRepository extends JpaRepository<ServiceTask, Long> {
    // Spring Data JPA provides implementations for standard CRUD operations:
    // - save(entity)
    // - findById(id)
    // - findAll()
    // - deleteById(id)
    // - existsById(id)
    // Additional custom query methods can be added here if needed
    
    /**
     * Find all tasks by status, ordered by creation date descending (newest first)
     * 
     * @param status the task status to filter by
     * @return list of tasks with the specified status
     */
    List<ServiceTask> findByStatusOrderByCreatedAtDesc(TaskStatus status);
    
    /**
     * Find all tasks ordered by creation date descending (newest first)
     * 
     * @return list of all tasks ordered by creation date
     */
    List<ServiceTask> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find all tasks assigned to a specific technician
     * Sorted by priority (descending - HIGH to LOW) and creation date (ascending - oldest first)
     * This provides tasks in order of importance and scheduled time for mobile workflow
     * 
     * @param technicianId the technician ID
     * @return list of tasks assigned to the technician
     */
    List<ServiceTask> findByAssignedToOrderByPriorityDescCreatedAtAsc(Long technicianId);
    
    /**
     * Find all tasks assigned to a specific technician with a specific status
     * Sorted by priority (descending - HIGH to LOW) and creation date (ascending - oldest first)
     * This provides filtered tasks in order of importance and scheduled time for mobile workflow
     * 
     * @param technicianId the technician ID
     * @param status the task status to filter by
     * @return list of tasks assigned to the technician with the specified status
     */
    List<ServiceTask> findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(Long technicianId, TaskStatus status);
    
    /**
     * Find all tasks created within a date range
     * 
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return list of tasks created within the date range
     */
    List<ServiceTask> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find completed tasks within a date range
     * Used for calculating average completion time
     * 
     * @param status the task status (COMPLETED)
     * @param startDate the start of the date range (inclusive)
     * @param endDate the end of the date range (inclusive)
     * @return list of completed tasks within the date range
     */
    List<ServiceTask> findByStatusAndCreatedAtBetween(TaskStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
