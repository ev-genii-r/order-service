package com.innowise.rudkovskii.integration.fallback;

import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.integration.client.UserClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UserServiceFallback implements UserClient {

    @Override
    public UserInfoDto getUserById(Long userId) {
        UserInfoDto user = new UserInfoDto();
        user.setId(userId);
        user.setEmail("Unknown (fallback)");
        user.setName("Unknown (fallback)");
        user.setSurname("Unknown (fallback)");
        user.setBirthDate(LocalDate.now());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
