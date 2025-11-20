package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
