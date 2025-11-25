package com.innowise.rudkovskii.service;

import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.integration.client.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserIntegrationServiceTest {

    @Mock
    private UserClient userServiceClient;

    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    private UserIntegrationService userIntegrationService;
    private UserInfoDto testUser;
    private Long userId;

    @BeforeEach
    void setUp() {
        userIntegrationService = new UserIntegrationService(userServiceClient, circuitBreakerFactory);

        userId = 1L;
        testUser = new UserInfoDto();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
    }

    @Test
    void getUserTest() {

        when(circuitBreakerFactory.create("user-service")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            var supplier = invocation.getArgument(0, Supplier.class);
            return supplier.get();
        });
        when(userServiceClient.getUserById(userId)).thenReturn(testUser);

        UserInfoDto result = userIntegrationService.getUser(userId);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(circuitBreakerFactory).create("user-service");
        verify(userServiceClient).getUserById(userId);
    }

    @Test
    void getUserCallFallbackTest() {

        when(circuitBreakerFactory.create("user-service")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            var fallback = invocation.getArgument(1, Function.class);
            return fallback.apply(new RuntimeException("Service unavailable"));
        });

        UserInfoDto result = userIntegrationService.getUser(userId);

        assertNotNull(result);
        verify(circuitBreakerFactory).create("user-service");
        verify(userServiceClient, never()).getUserById(anyLong());
    }

    @Test
    void getUserCircuitBreakerNameTest() {

        when(circuitBreakerFactory.create("user-service")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            var supplier = invocation.getArgument(0, Supplier.class);
            return supplier.get();
        });
        when(userServiceClient.getUserById(userId)).thenReturn(testUser);

        userIntegrationService.getUser(userId);

        verify(circuitBreakerFactory, times(1)).create("user-service");
        verify(circuitBreakerFactory, never()).create("wrong-service-name");
    }
}