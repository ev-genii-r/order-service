package com.innowise.rudkovskii.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.rudkovskii.dto.order.OrderDto;
import com.innowise.rudkovskii.dto.order.OrderWithUserDto;
import com.innowise.rudkovskii.dto.orderItem.OrderItemDto;
import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.entity.Item;
import com.innowise.rudkovskii.entity.Order;
import com.innowise.rudkovskii.exception.ResourceNotFoundException;
import com.innowise.rudkovskii.repository.ItemRepository;
import com.innowise.rudkovskii.repository.OrderRepository;
import com.innowise.rudkovskii.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("user.service.base-url", wireMockServer::baseUrl);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.enable_lazy_load_no_trans", () -> "true");
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        orderRepository.deleteAll();
    }

    @Test
    void createOrderTest() throws Exception {
        OrderDto orderRequest = createOrderDto(null, 200L, "CREATED", List.of(
                createOrderItemDto(null, 101L, 2)
        ));

        UserInfoDto userInfo = createUserInfoDto(200L, "jane.smith@example.com", "Jane", "Smith",
                LocalDate.of(1990, 5, 15), LocalDateTime.now().minusDays(10));

        wireMockServer.stubFor(get(urlEqualTo("/api/users/200"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        OrderWithUserDto result = orderService.createOrder(orderRequest);

        assertNotNull(result);
        assertNotNull(result.getOrder().getId());
        assertEquals("CREATED", result.getOrder().getStatus());

        assertTrue(orderRepository.findById(result.getOrder().getId()).isPresent());

        verify(getRequestedFor(urlEqualTo("/api/users/200")));
    }

    @Test
    void getOrderTest() throws Exception {

        OrderDto orderRequest = createOrderDto(null, 100L, "PENDING", List.of(
                createOrderItemDto(null, 102L, 1)
        ));

        UserInfoDto userInfo = createUserInfoDto(100L, "john.doe@example.com", "John", "Doe",
                LocalDate.of(1985, 3, 20), LocalDateTime.now().minusMonths(1));

        wireMockServer.stubFor(get(urlEqualTo("/api/users/100"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        OrderWithUserDto createdOrder = orderService.createOrder(orderRequest);

        wireMockServer.resetRequests();

        wireMockServer.stubFor(get(urlEqualTo("/api/users/100"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        OrderWithUserDto result = orderService.getOrder(createdOrder.getOrder().getId());

        assertNotNull(result);
        assertNotNull(result.getOrder());
        assertNotNull(result.getUser());
        assertEquals(createdOrder.getOrder().getId(), result.getOrder().getId());
        assertEquals("John", result.getUser().getName());
        assertEquals("john.doe@example.com", result.getUser().getEmail());
        assertEquals("PENDING", result.getOrder().getStatus());

        verify(getRequestedFor(urlEqualTo("/api/users/100")));
    }

    @Test
    void getOrderFoundTest() {

        Long nonExistentOrderId = 999L;

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrder(nonExistentOrderId));
    }

    @Test
    void getOrdersByStatusTest() throws Exception {

        UserInfoDto user1 = createUserInfoDto(300L, "user300@example.com", "User", "Three Hundred",
                LocalDate.of(1992, 7, 10), LocalDateTime.now().minusDays(5));
        UserInfoDto user2 = createUserInfoDto(301L, "user301@example.com", "User", "Three Hundred One",
                LocalDate.of(1988, 12, 3), LocalDateTime.now().minusDays(3));
        UserInfoDto user3 = createUserInfoDto(302L, "user302@example.com", "User", "Three Hundred Two",
                LocalDate.of(1995, 4, 25), LocalDateTime.now().minusDays(1));

        wireMockServer.stubFor(get(urlEqualTo("/api/users/300"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user1))));
        wireMockServer.stubFor(get(urlEqualTo("/api/users/301"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user2))));
        wireMockServer.stubFor(get(urlEqualTo("/api/users/302"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user3))));

        OrderDto order1Request = createOrderDto(null, 300L, "PENDING", List.of(
                createOrderItemDto(null, 201L, 1)
        ));
        OrderDto order2Request = createOrderDto(null, 301L, "COMPLETED", List.of(
                createOrderItemDto(null, 202L, 3)
        ));
        OrderDto order3Request = createOrderDto(null, 302L, "PENDING", List.of(
                createOrderItemDto(null, 203L, 2)
        ));

        orderService.createOrder(order1Request);
        orderService.createOrder(order2Request);
        orderService.createOrder(order3Request);

        wireMockServer.resetRequests();

        wireMockServer.stubFor(get(urlEqualTo("/api/users/300"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user1))));
        wireMockServer.stubFor(get(urlEqualTo("/api/users/302"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user3))));

        List<OrderWithUserDto> result = orderService.getOrdersByStatus(List.of("PENDING"));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(dto -> dto.getOrder().getStatus()).containsOnly("PENDING");
        verify(getRequestedFor(urlEqualTo("/api/users/300")));
        verify(getRequestedFor(urlEqualTo("/api/users/302")));
        verify(0, getRequestedFor(urlEqualTo("/api/users/301")));
    }

    @Test
    void updateOrderTest() throws Exception {

        Item item = new Item();
        item.setName("Test Item");
        item.setPrice(10.0);
        item = itemRepository.save(item);

        Long userId = 400L;

        UserInfoDto originalUser = createUserInfoDto(
                userId, "original@example.com", "Original", "User",
                LocalDate.of(1990, 1, 1), LocalDateTime.now().minusYears(2)
        );

        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(originalUser))));

        OrderDto createRequest = createOrderDto(
                null,
                userId,
                "PENDING",
                List.of(createOrderItemDto(null, item.getId(), 1))
        );

        OrderWithUserDto createdOrder = orderService.createOrder(createRequest);
        Long orderId = createdOrder.getOrder().getId();

        wireMockServer.resetRequests();

        UserInfoDto updatedUser = createUserInfoDto(
                userId, "updated@example.com", "Updated", "User",
                LocalDate.of(1990, 1, 1), LocalDateTime.now().minusYears(2)
        );

        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(updatedUser))));

        OrderDto updateRequest = createOrderDto(
                null,
                userId,
                "COMPLETED",
                List.of(createOrderItemDto(null, item.getId(), 2)) // новое количество
        );

        OrderWithUserDto result = orderService.updateOrder(orderId, updateRequest);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getOrder().getStatus());
        assertEquals(userId, result.getOrder().getUserId());
        assertEquals("Updated", result.getUser().getName());
        assertEquals("updated@example.com", result.getUser().getEmail());

        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals("COMPLETED", updatedOrder.getStatus());
        assertEquals(1, updatedOrder.getItems().size());
        assertEquals(2, updatedOrder.getItems().get(0).getQuantity());

        verify(getRequestedFor(urlEqualTo("/api/users/" + userId)));
    }


    @Test
    void getOrdersByIdsTest() throws Exception {

        UserInfoDto user1 = createUserInfoDto(500L, "user500@example.com", "User", "Five Hundred",
                LocalDate.of(1987, 6, 15), LocalDateTime.now().minusMonths(6));
        UserInfoDto user2 = createUserInfoDto(501L, "user501@example.com", "User", "Five Hundred One",
                LocalDate.of(1993, 9, 22), LocalDateTime.now().minusMonths(3));
        UserInfoDto user3 = createUserInfoDto(502L, "user502@example.com", "User", "Five Hundred Two",
                LocalDate.of(1985, 11, 8), LocalDateTime.now().minusMonths(1));

        wireMockServer.stubFor(get(urlEqualTo("/api/users/500"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user1))));
        wireMockServer.stubFor(get(urlEqualTo("/api/users/501"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user2))));
        wireMockServer.stubFor(get(urlEqualTo("/api/users/502"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user3))));

        OrderDto order1Request = createOrderDto(null, 500L, "PENDING", List.of(
                createOrderItemDto(null, 401L, 1)
        ));
        OrderDto order2Request = createOrderDto(null, 501L, "SHIPPED", List.of(
                createOrderItemDto(null, 402L, 2)
        ));
        OrderDto order3Request = createOrderDto(null, 502L, "DELIVERED", List.of(
                createOrderItemDto(null, 403L, 1)
        ));

        OrderWithUserDto order1 = orderService.createOrder(order1Request);
        OrderWithUserDto order2 = orderService.createOrder(order2Request);
        OrderWithUserDto order3 = orderService.createOrder(order3Request);

        List<Long> orderIds = List.of(order1.getOrder().getId(), order2.getOrder().getId(), order3.getOrder().getId());

        wireMockServer.resetRequests();
        wireMockServer.stubFor(get(urlEqualTo("/api/users/500"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user1))));
        wireMockServer.stubFor(get(urlEqualTo("/api/users/501"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user2))));
        wireMockServer.stubFor(get(urlEqualTo("/api/users/502"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user3))));

        List<OrderWithUserDto> result = orderService.getOrdersByIds(orderIds);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(dto -> dto.getOrder().getId()).containsExactlyInAnyOrderElementsOf(orderIds);
        assertThat(result).extracting(dto -> dto.getUser().getName()).containsOnly("User");
        assertThat(result).extracting(dto -> dto.getUser().getSurname())
                .containsExactlyInAnyOrder("Five Hundred", "Five Hundred One", "Five Hundred Two");

        verify(getRequestedFor(urlEqualTo("/api/users/500")));
        verify(getRequestedFor(urlEqualTo("/api/users/501")));
        verify(getRequestedFor(urlEqualTo("/api/users/502")));
    }

    @Test
    void deleteOrderTest() throws JsonProcessingException {

        UserInfoDto userInfo = createUserInfoDto(600L, "user600@example.com", "User", "Six Hundred",
                LocalDate.of(1991, 2, 14), LocalDateTime.now().minusWeeks(2));

        wireMockServer.stubFor(get(urlEqualTo("/api/users/600"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        OrderDto orderRequest = createOrderDto(null, 600L, "PENDING", List.of(
                createOrderItemDto(null, 501L, 1)
        ));
        OrderWithUserDto createdOrder = orderService.createOrder(orderRequest);

        orderService.deleteOrder(createdOrder.getOrder().getId());

        assertFalse(orderRepository.existsById(createdOrder.getOrder().getId()));
    }

    @Test
    void deleteOrderNotFoundTest() {

        Long nonExistentOrderId = 999L;

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.deleteOrder(nonExistentOrderId));
    }

    @Test
    void getOrdersByStatusNotFoundTest() {

        List<OrderWithUserDto> result = orderService.getOrdersByStatus(List.of("NON_EXISTENT_STATUS"));

        assertThat(result).isEmpty();
    }

    private OrderDto createOrderDto(Long id, Long userId, String status, List<OrderItemDto> items) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(id);
        orderDto.setUserId(userId);
        orderDto.setStatus(status);
        orderDto.setCreationDate(LocalDateTime.now());
        orderDto.setItems(items);
        return orderDto;
    }

    private OrderItemDto createOrderItemDto(Long id, Long itemId, Integer quantity) {
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setId(id);
        orderItemDto.setItemId(itemId);
        orderItemDto.setQuantity(quantity);
        return orderItemDto;
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
