package com.fsm.notification.repository;

import com.fsm.notification.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DeviceToken persistence
 */
@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    /**
     * Find all active device tokens for a user
     * 
     * @param userId the user ID
     * @return list of active device tokens
     */
    List<DeviceToken> findByUserIdAndActiveTrue(Long userId);
    
    /**
     * Find device token by user ID and device ID
     * 
     * @param userId the user ID
     * @param deviceId the device ID
     * @return optional device token
     */
    Optional<DeviceToken> findByUserIdAndDeviceId(Long userId, String deviceId);
    
    /**
     * Find all device tokens for a user
     * 
     * @param userId the user ID
     * @return list of device tokens
     */
    List<DeviceToken> findByUserId(Long userId);
}
