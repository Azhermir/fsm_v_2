package com.fsm.user.repository;

import com.fsm.user.domain.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for Technician entity
 */
@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    
    /**
     * Find a technician by email address
     * 
     * @param email the email address
     * @return Optional containing the technician if found
     */
    Optional<Technician> findByEmail(String email);
}
