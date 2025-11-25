package com.innowise.rudkovskii.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.service.UserIntegrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class UserIntegrationServiceIntegrationTest {

    private WireMockServer wireMockServer;

    @Autowired
    private UserIntegrationService userIntegrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("user-service")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("user.service.base-url", () -> "http://localhost:8089");
    }

    @Test
    void getUserTest() throws Exception {

        Long userId = 1L;
        UserInfoDto expectedUser = createUserInfoDto(userId, "test@example.com", "John", "Doe",
                LocalDate.of(1990, 1, 1), LocalDateTime.now().minusMonths(1));

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(expectedUser))));

        UserInfoDto result = userIntegrationService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John", result.getName());
        assertEquals("Doe", result.getSurname());
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getBirthDate());
        assertNotNull(result.getCreatedAt());

        verify(getRequestedFor(urlEqualTo("/api/users/" + userId)));
    }

    @Test
    void getUserServiceReturnsErrorTest() throws Exception {

        Long userId = 2L;

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

        UserInfoDto result = userIntegrationService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Unknown (fallback)", result.getName());
        assertEquals("Unknown (fallback)", result.getSurname());
        assertTrue(result.getEmail().contains("Unknown (fallback)"));
        assertNotNull(result.getBirthDate());
        assertNotNull(result.getCreatedAt());

        verify(getRequestedFor(urlEqualTo("/api/users/" + userId)));
    }

    private UserInfoDto createUserInfoDto(Long id, String email, String name, String surname,
                                          LocalDate birthDate, LocalDateTime createdAt) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setId(id);
        userInfoDto.setEmail(email);
        userInfoDto.setName(name);
        userInfoDto.setSurname(surname);
        userInfoDto.setBirthDate(birthDate);
        userInfoDto.setCreatedAt(createdAt);
        return userInfoDto;
    }
}
