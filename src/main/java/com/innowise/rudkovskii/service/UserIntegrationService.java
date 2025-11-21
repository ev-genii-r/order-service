package com.innowise.rudkovskii.service;

import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.integration.client.UserClient;
import com.innowise.rudkovskii.integration.fallback.UserServiceFallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserIntegrationService {

    private final UserClient userServiceClient;
    private final CircuitBreakerFactory<?, ?> cbFactory;

    public UserInfoDto getUser(Long userId) {
        var cb = cbFactory.create("user-service");

        return cb.run(
                () -> {
                    log.info("Calling user service for user ID: {}", userId);
                    return userServiceClient.getUserById(userId);
                },
                throwable -> {
                    log.info("Fallback for user ID: {}, error: {}", userId, throwable.getMessage());
                    return fallback(userId);
                }
        );
    }

    private UserInfoDto fallback(Long userId) {
        UserServiceFallback fallback = new UserServiceFallback();
        return fallback.getUserById(userId);
    }
}
