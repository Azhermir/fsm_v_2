package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

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
}
