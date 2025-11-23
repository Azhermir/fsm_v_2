package com.fsm.notification.service;

import com.fsm.notification.domain.DeviceToken;
import com.fsm.notification.dto.DeviceTokenRequest;
import com.fsm.notification.dto.DeviceTokenResponse;
import com.fsm.notification.repository.DeviceTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeviceTokenService
 */
@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {
    
    @Mock
    private DeviceTokenRepository deviceTokenRepository;
    
    @InjectMocks
    private DeviceTokenService deviceTokenService;
    
    private DeviceTokenRequest testRequest;
    private DeviceToken testToken;
    
    @BeforeEach
    void setUp() {
        testRequest = DeviceTokenRequest.builder()
                .userId(1L)
                .fcmToken("test-fcm-token-123")
                .deviceId("device-001")
                .platform("Android")
                .build();
        
        testToken = DeviceToken.createDeviceToken(1L, "test-fcm-token-123", "device-001", "Android");
        testToken.setId(1L);
    }
    
    @Test
    void testRegisterDeviceToken_NewToken_Success() {
        // Given
        when(deviceTokenRepository.findByUserIdAndDeviceId(1L, "device-001"))
                .thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(invocation -> {
                    DeviceToken token = invocation.getArgument(0);
                    token.setId(1L);
                    return token;
                });
        
        // When
        DeviceTokenResponse response = deviceTokenService.registerDeviceToken(testRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("device-001", response.getDeviceId());
        assertEquals("Android", response.getPlatform());
        assertTrue(response.getActive());
        
        verify(deviceTokenRepository).findByUserIdAndDeviceId(1L, "device-001");
        verify(deviceTokenRepository).save(any(DeviceToken.class));
    }
    
    @Test
    void testRegisterDeviceToken_ExistingToken_UpdatesToken() {
        // Given
        DeviceToken existingToken = DeviceToken.createDeviceToken(
                1L, "old-fcm-token", "device-001", "Android");
        existingToken.setId(1L);
        
        when(deviceTokenRepository.findByUserIdAndDeviceId(1L, "device-001"))
                .thenReturn(Optional.of(existingToken));
        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        DeviceTokenResponse response = deviceTokenService.registerDeviceToken(testRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        
        ArgumentCaptor<DeviceToken> tokenCaptor = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepository).save(tokenCaptor.capture());
        
        DeviceToken savedToken = tokenCaptor.getValue();
        assertEquals("test-fcm-token-123", savedToken.getFcmToken());
        assertTrue(savedToken.getActive());
    }
    
    @Test
    void testRegisterDeviceToken_ReactivatesInactiveToken() {
        // Given
        DeviceToken existingToken = DeviceToken.createDeviceToken(
                1L, "old-fcm-token", "device-001", "Android");
        existingToken.setId(1L);
        existingToken.deactivate();
        
        when(deviceTokenRepository.findByUserIdAndDeviceId(1L, "device-001"))
                .thenReturn(Optional.of(existingToken));
        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        DeviceTokenResponse response = deviceTokenService.registerDeviceToken(testRequest);
        
        // Then
        assertTrue(response.getActive());
        
        ArgumentCaptor<DeviceToken> tokenCaptor = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepository).save(tokenCaptor.capture());
        
        DeviceToken savedToken = tokenCaptor.getValue();
        assertTrue(savedToken.getActive());
    }
    
    @Test
    void testRegisterDeviceToken_UpdatesPlatform() {
        // Given
        DeviceToken existingToken = DeviceToken.createDeviceToken(
                1L, "old-fcm-token", "device-001", "Android");
        existingToken.setId(1L);
        
        testRequest.setPlatform("iOS");
        
        when(deviceTokenRepository.findByUserIdAndDeviceId(1L, "device-001"))
                .thenReturn(Optional.of(existingToken));
        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        deviceTokenService.registerDeviceToken(testRequest);
        
        // Then
        ArgumentCaptor<DeviceToken> tokenCaptor = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepository).save(tokenCaptor.capture());
        
        DeviceToken savedToken = tokenCaptor.getValue();
        assertEquals("iOS", savedToken.getPlatform());
    }
    
    @Test
    void testUnregisterDeviceToken_Success() {
        // Given
        when(deviceTokenRepository.findByUserIdAndDeviceId(1L, "device-001"))
                .thenReturn(Optional.of(testToken));
        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        deviceTokenService.unregisterDeviceToken(1L, "device-001");
        
        // Then
        ArgumentCaptor<DeviceToken> tokenCaptor = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepository).save(tokenCaptor.capture());
        
        DeviceToken savedToken = tokenCaptor.getValue();
        assertFalse(savedToken.getActive());
    }
    
    @Test
    void testUnregisterDeviceToken_NotFound_ThrowsException() {
        // Given
        when(deviceTokenRepository.findByUserIdAndDeviceId(1L, "device-001"))
                .thenReturn(Optional.empty());
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () ->
                deviceTokenService.unregisterDeviceToken(1L, "device-001")
        );
        
        verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
    }
    
    @Test
    void testGetActiveDeviceTokens_ReturnsActiveTokensOnly() {
        // Given
        DeviceToken activeToken1 = DeviceToken.createDeviceToken(
                1L, "token1", "device-001", "Android");
        activeToken1.setId(1L);
        
        DeviceToken activeToken2 = DeviceToken.createDeviceToken(
                1L, "token2", "device-002", "iOS");
        activeToken2.setId(2L);
        
        when(deviceTokenRepository.findByUserIdAndActiveTrue(1L))
                .thenReturn(Arrays.asList(activeToken1, activeToken2));
        
        // When
        List<DeviceTokenResponse> responses = deviceTokenService.getActiveDeviceTokens(1L);
        
        // Then
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getActive()));
        assertTrue(responses.stream().allMatch(r -> r.getUserId().equals(1L)));
        
        verify(deviceTokenRepository).findByUserIdAndActiveTrue(1L);
    }
    
    @Test
    void testGetActiveDeviceTokens_EmptyList() {
        // Given
        when(deviceTokenRepository.findByUserIdAndActiveTrue(999L))
                .thenReturn(Collections.emptyList());
        
        // When
        List<DeviceTokenResponse> responses = deviceTokenService.getActiveDeviceTokens(999L);
        
        // Then
        assertEquals(0, responses.size());
        verify(deviceTokenRepository).findByUserIdAndActiveTrue(999L);
    }
    
    @Test
    void testGetActiveFcmTokens_ReturnsTokenStrings() {
        // Given
        DeviceToken token1 = DeviceToken.createDeviceToken(
                1L, "fcm-token-1", "device-001", "Android");
        token1.setId(1L);
        
        DeviceToken token2 = DeviceToken.createDeviceToken(
                1L, "fcm-token-2", "device-002", "iOS");
        token2.setId(2L);
        
        when(deviceTokenRepository.findByUserIdAndActiveTrue(1L))
                .thenReturn(Arrays.asList(token1, token2));
        
        // When
        List<String> fcmTokens = deviceTokenService.getActiveFcmTokens(1L);
        
        // Then
        assertEquals(2, fcmTokens.size());
        assertTrue(fcmTokens.contains("fcm-token-1"));
        assertTrue(fcmTokens.contains("fcm-token-2"));
        
        verify(deviceTokenRepository).findByUserIdAndActiveTrue(1L);
    }
    
    @Test
    void testGetActiveFcmTokens_EmptyList() {
        // Given
        when(deviceTokenRepository.findByUserIdAndActiveTrue(999L))
                .thenReturn(Collections.emptyList());
        
        // When
        List<String> fcmTokens = deviceTokenService.getActiveFcmTokens(999L);
        
        // Then
        assertEquals(0, fcmTokens.size());
    }
    
    @Test
    void testRegisterDeviceToken_WithNullPlatform() {
        // Given
        testRequest.setPlatform(null);
        
        when(deviceTokenRepository.findByUserIdAndDeviceId(1L, "device-001"))
                .thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any(DeviceToken.class)))
                .thenAnswer(invocation -> {
                    DeviceToken token = invocation.getArgument(0);
                    token.setId(1L);
                    return token;
                });
        
        // When
        DeviceTokenResponse response = deviceTokenService.registerDeviceToken(testRequest);
        
        // Then
        assertNotNull(response);
        assertNull(response.getPlatform());
    }
}
