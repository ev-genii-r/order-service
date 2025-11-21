package com.innowise.rudkovskii.integration.client;

import com.innowise.rudkovskii.config.FeignConfig;
import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.integration.fallback.UserServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8081",
        fallback = UserServiceFallback.class,
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserInfoDto getUserById(@PathVariable("id") Long userId);

}
