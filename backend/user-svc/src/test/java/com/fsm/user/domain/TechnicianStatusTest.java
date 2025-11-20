package com.fsm.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnicianStatus enum
 */
class TechnicianStatusTest {
    
    @Test
    @DisplayName("Should have AVAILABLE status")
    void shouldHaveAvailableStatus() {
        // Act
        TechnicianStatus status = TechnicianStatus.AVAILABLE;
        
        // Assert
        assertNotNull(status);
        assertEquals("AVAILABLE", status.name());
    }
    
    @Test
    @DisplayName("Should have BUSY status")
    void shouldHaveBusyStatus() {
        // Act
        TechnicianStatus status = TechnicianStatus.BUSY;
        
        // Assert
        assertNotNull(status);
        assertEquals("BUSY", status.name());
    }
    
    @Test
    @DisplayName("Should have OFFLINE status")
    void shouldHaveOfflineStatus() {
        // Act
        TechnicianStatus status = TechnicianStatus.OFFLINE;
        
        // Assert
        assertNotNull(status);
        assertEquals("OFFLINE", status.name());
    }
    
    @Test
    @DisplayName("Should have exactly three status values")
    void shouldHaveExactlyThreeStatusValues() {
        // Act
        TechnicianStatus[] statuses = TechnicianStatus.values();
        
        // Assert
        assertEquals(3, statuses.length);
    }
    
    @Test
    @DisplayName("Should convert string to enum")
    void shouldConvertStringToEnum() {
        // Act
        TechnicianStatus available = TechnicianStatus.valueOf("AVAILABLE");
        TechnicianStatus busy = TechnicianStatus.valueOf("BUSY");
        TechnicianStatus offline = TechnicianStatus.valueOf("OFFLINE");
        
        // Assert
        assertEquals(TechnicianStatus.AVAILABLE, available);
        assertEquals(TechnicianStatus.BUSY, busy);
        assertEquals(TechnicianStatus.OFFLINE, offline);
    }
}
