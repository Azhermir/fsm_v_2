package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Database implementation of IServiceTaskRepository using Spring Data JPA
 * This is the primary repository implementation for production use
 */
@Repository
@Primary
public class DatabaseServiceTaskRepository implements IServiceTaskRepository {
    
    private final ServiceTaskJpaRepository jpaRepository;
    
    @Autowired
    public DatabaseServiceTaskRepository(ServiceTaskJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public ServiceTask save(ServiceTask serviceTask) {
        if (serviceTask == null) {
            throw new IllegalArgumentException("ServiceTask cannot be null");
        }
        return jpaRepository.save(serviceTask);
    }
    
    @Override
    public Optional<ServiceTask> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<ServiceTask> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public List<ServiceTask> findAllOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Override
    public List<ServiceTask> findByStatusOrderByCreatedAtDesc(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("TaskStatus cannot be null");
        }
        return jpaRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Override
    public List<ServiceTask> findByAssignedToOrderByPriorityDescCreatedAtAsc(Long technicianId) {
        if (technicianId == null) {
            throw new IllegalArgumentException("Technician ID cannot be null");
        }
        return jpaRepository.findByAssignedToOrderByPriorityDescCreatedAtAsc(technicianId);
    }
    
    @Override
    public List<ServiceTask> findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(Long technicianId, TaskStatus status) {
        if (technicianId == null) {
            throw new IllegalArgumentException("Technician ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("TaskStatus cannot be null");
        }
        return jpaRepository.findByAssignedToAndStatusOrderByPriorityDescCreatedAtAsc(technicianId, status);
    }
    
    @Override
    public List<ServiceTask> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        return jpaRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    @Override
    public List<ServiceTask> findByStatusAndCreatedAtBetween(TaskStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        if (status == null) {
            throw new IllegalArgumentException("TaskStatus cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        return jpaRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate);
    }
}
