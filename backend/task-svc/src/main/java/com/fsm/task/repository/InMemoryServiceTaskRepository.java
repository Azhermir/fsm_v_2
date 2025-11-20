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
        ServiceTask task1 = ServiceTask.createServiceTask(
                "Fix HVAC System",
                "Air conditioning not working properly",
                "123 Main St, Springfield, IL 62701",
                Priority.HIGH,
                120);
        task1 = assignId(task1, idGenerator.getAndIncrement());
        task1 = ServiceTask.builder()
                .id(task1.getId())
                .title(task1.getTitle())
                .description(task1.getDescription())
                .clientAddress(task1.getClientAddress())
                .priority(task1.getPriority())
                .estimatedDuration(task1.getEstimatedDuration())
                .status(TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        
        ServiceTask task2 = ServiceTask.createServiceTask(
                "Plumbing Inspection",
                "Routine plumbing inspection for commercial building",
                "456 Oak Ave, Chicago, IL 60601",
                Priority.MEDIUM,
                90);
        task2 = assignId(task2, idGenerator.getAndIncrement());
        task2 = ServiceTask.builder()
                .id(task2.getId())
                .title(task2.getTitle())
                .description(task2.getDescription())
                .clientAddress(task2.getClientAddress())
                .priority(task2.getPriority())
                .estimatedDuration(task2.getEstimatedDuration())
                .status(TaskStatus.ASSIGNED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        ServiceTask task3 = ServiceTask.createServiceTask(
                "Emergency Electrical Repair",
                "Power outage in office building",
                "789 Elm St, Boston, MA 02101",
                Priority.CRITICAL,
                180);
        task3 = assignId(task3, idGenerator.getAndIncrement());
        task3 = ServiceTask.builder()
                .id(task3.getId())
                .title(task3.getTitle())
                .description(task3.getDescription())
                .clientAddress(task3.getClientAddress())
                .priority(task3.getPriority())
                .estimatedDuration(task3.getEstimatedDuration())
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now().minusHours(6))
                .build();
        
        storage.put(task1.getId(), task1);
        storage.put(task2.getId(), task2);
        storage.put(task3.getId(), task3);
    }
    
    /**
     * Helper method to assign an ID to a task
     */
    private ServiceTask assignId(ServiceTask task, Long id) {
        return ServiceTask.builder()
                .id(id)
                .title(task.getTitle())
                .description(task.getDescription())
                .clientAddress(task.getClientAddress())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt() != null ? 
                        task.getCreatedAt() : LocalDateTime.now())
                .build();
    }
    
    @Override
    public ServiceTask save(ServiceTask serviceTask) {
        if (serviceTask == null) {
            throw new IllegalArgumentException("ServiceTask cannot be null");
        }
        
        // If no ID, generate one (new entity)
        if (serviceTask.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            ServiceTask taskWithId = assignId(serviceTask, newId);
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
