package com.fsm.taskmanagement.repository;

import com.fsm.taskmanagement.entity.Priority;
import com.fsm.taskmanagement.entity.ServiceTask;
import com.fsm.taskmanagement.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceTaskRepository extends JpaRepository<ServiceTask, UUID> {
    // JpaRepository provides basic CRUD operations:
    // - save(entity): saves/updates an entity
    // - findById(id): retrieves an entity by ID
    // - findAll(): retrieves all entities
    // - deleteById(id): deletes an entity by ID
    // Additional custom query methods can be added here if needed
    
    // Dashboard statistics queries
    long countByStatus(Status status);
    
    long countByStatusAndCreatedAtAfter(Status status, LocalDateTime startDate);
    
    long countByCreatedAtAfter(LocalDateTime startDate);
    
    long countByPriority(Priority priority);
    
    long countByPriorityAndCreatedAtAfter(Priority priority, LocalDateTime startDate);
    
    List<ServiceTask> findByCreatedAtAfter(LocalDateTime startDate);
}
