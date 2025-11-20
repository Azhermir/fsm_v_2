package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
