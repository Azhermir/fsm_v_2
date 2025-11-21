package com.fsm.task.repository;

import com.fsm.task.domain.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for TaskAssignment entity
 */
@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    
    /**
     * Find the latest assignment for a given task
     * 
     * @param taskId the task ID
     * @return the latest assignment for the task, if any
     */
    Optional<TaskAssignment> findTopByTaskIdOrderByAssignedAtDesc(Long taskId);
    
    /**
     * Find all assignments for a given task ordered by assignment time (newest first)
     * This provides the reassignment history for audit purposes
     * 
     * @param taskId the task ID
     * @return list of all assignments for the task (reassignment history)
     */
    java.util.List<TaskAssignment> findByTaskIdOrderByAssignedAtDesc(Long taskId);
    
    /**
     * Find all assignments for a given technician
     * 
     * @param technicianId the technician ID
     * @return list of assignments for the technician
     */
    java.util.List<TaskAssignment> findByTechnicianId(Long technicianId);
}
