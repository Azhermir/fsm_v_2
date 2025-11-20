package com.fsm.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRole enum
 */
class UserRoleTest {
    
    @Test
    @DisplayName("Should have DISPATCHER role")
    void shouldHaveDispatcherRole() {
        UserRole role = UserRole.DISPATCHER;
        assertNotNull(role);
        assertEquals("DISPATCHER", role.name());
    }
    
    @Test
    @DisplayName("Should have TECHNICIAN role")
    void shouldHaveTechnicianRole() {
        UserRole role = UserRole.TECHNICIAN;
        assertNotNull(role);
        assertEquals("TECHNICIAN", role.name());
    }
    
    @Test
    @DisplayName("Should have CUSTOMER role")
    void shouldHaveCustomerRole() {
        UserRole role = UserRole.CUSTOMER;
        assertNotNull(role);
        assertEquals("CUSTOMER", role.name());
    }
    
    @Test
    @DisplayName("Should have SUPERVISOR role")
    void shouldHaveSupervisorRole() {
        UserRole role = UserRole.SUPERVISOR;
        assertNotNull(role);
        assertEquals("SUPERVISOR", role.name());
    }
    
    @Test
    @DisplayName("Should have exactly four role values")
    void shouldHaveExactlyFourRoleValues() {
        UserRole[] roles = UserRole.values();
        assertEquals(4, roles.length);
    }
    
    @Test
    @DisplayName("Should convert string to enum")
    void shouldConvertStringToEnum() {
        UserRole dispatcher = UserRole.valueOf("DISPATCHER");
        UserRole technician = UserRole.valueOf("TECHNICIAN");
        UserRole customer = UserRole.valueOf("CUSTOMER");
        UserRole supervisor = UserRole.valueOf("SUPERVISOR");
        
        assertEquals(UserRole.DISPATCHER, dispatcher);
        assertEquals(UserRole.TECHNICIAN, technician);
        assertEquals(UserRole.CUSTOMER, customer);
        assertEquals(UserRole.SUPERVISOR, supervisor);
    }
}
