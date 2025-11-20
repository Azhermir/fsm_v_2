package com.fsm.task.repository;

import com.fsm.task.domain.ServiceTask;
import com.fsm.task.domain.Priority;
import com.fsm.task.domain.TaskStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of IServiceTaskRepository for testing
 * Uses ConcurrentHashMap for thread-safe operations
 */
@Repository
public class InMemoryServiceTaskRepository implements IServiceTaskRepository {
    
    private final Map<Long, ServiceTask> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public InMemoryServiceTaskRepository() {
        // Initialize with some mock/static data
        initializeMockData();
    }
    
    /**
     * Initialize repository with mock data for testing
     */
    private void initializeMockData() {
        ServiceTask task1 = ServiceTask.builder()
                .id(idGenerator.getAndIncrement())
                .title("Fix HVAC System")
                .description("Air conditioning not working properly")
                .clientAddress("123 Main St, Springfield, IL 62701")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .id(idGenerator.getAndIncrement())
                .title("Plumbing Inspection")
                .description("Routine plumbing inspection for commercial building")
                .clientAddress("456 Oak Ave, Chicago, IL 60601")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.ASSIGNED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        ServiceTask task3 = ServiceTask.builder()
                .id(idGenerator.getAndIncrement())
                .title("Emergency Electrical Repair")
                .description("Power outage in office building")
                .clientAddress("789 Elm St, Boston, MA 02101")
                .priority(Priority.CRITICAL)
                .estimatedDuration(180)
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now().minusHours(6))
                .build();
        
        storage.put(task1.getId(), task1);
        storage.put(task2.getId(), task2);
        storage.put(task3.getId(), task3);
    }
    
    @Override
    public ServiceTask save(ServiceTask serviceTask) {
        if (serviceTask == null) {
            throw new IllegalArgumentException("ServiceTask cannot be null");
        }
        
        // If no ID, generate one (new entity)
        if (serviceTask.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            ServiceTask taskWithId = ServiceTask.builder()
                    .id(newId)
                    .title(serviceTask.getTitle())
                    .description(serviceTask.getDescription())
                    .clientAddress(serviceTask.getClientAddress())
                    .priority(serviceTask.getPriority())
                    .estimatedDuration(serviceTask.getEstimatedDuration())
                    .status(serviceTask.getStatus())
                    .createdAt(serviceTask.getCreatedAt() != null ? 
                            serviceTask.getCreatedAt() : LocalDateTime.now())
                    .build();
            storage.put(newId, taskWithId);
            return taskWithId;
        }
        
        // Update existing entity (idempotent operation)
        storage.put(serviceTask.getId(), serviceTask);
        return serviceTask;
    }
    
    @Override
    public Optional<ServiceTask> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public List<ServiceTask> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }
    
    /**
     * Clear all data from the repository (useful for testing)
     */
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }
    
    /**
     * Get the current size of the repository (useful for testing)
     */
    public int size() {
        return storage.size();
    }
}
