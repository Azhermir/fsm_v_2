package com.fsm.taskmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Title must not be empty")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description must not be empty")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Client address must not be empty")
    @Column(nullable = false)
    private String clientAddress;

    @NotNull(message = "Priority must be specified")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = true)
    private Integer estimatedDuration;

    @NotNull(message = "Status must be specified")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.UNASSIGNED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = Status.UNASSIGNED;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Domain invariant validation
     * Ensures that the ServiceTask entity maintains valid state
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description must not be empty");
        }
        if (clientAddress == null || clientAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Client address must not be empty");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority must be specified");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must be specified");
        }
    }
}
