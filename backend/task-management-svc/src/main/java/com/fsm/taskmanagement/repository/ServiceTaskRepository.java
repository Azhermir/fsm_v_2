package com.fsm.taskmanagement.repository;

import com.fsm.taskmanagement.entity.ServiceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceTaskRepository extends JpaRepository<ServiceTask, UUID> {
    // JpaRepository provides basic CRUD operations:
    // - save(entity): saves/updates an entity
    // - findById(id): retrieves an entity by ID
    // - findAll(): retrieves all entities
    // - deleteById(id): deletes an entity by ID
    // Additional custom query methods can be added here if needed
}
