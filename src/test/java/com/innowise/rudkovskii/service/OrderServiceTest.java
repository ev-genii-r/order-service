package com.innowise.rudkovskii.service;

import com.innowise.rudkovskii.dto.order.OrderDto;
import com.innowise.rudkovskii.dto.order.OrderMapper;
import com.innowise.rudkovskii.dto.order.OrderWithUserDto;
import com.innowise.rudkovskii.dto.orderItem.OrderItemDto;
import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.entity.Order;
import com.innowise.rudkovskii.exception.ResourceNotFoundException;
import com.innowise.rudkovskii.repository.ItemRepository;
import com.innowise.rudkovskii.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserIntegrationService userIntegration;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDto orderDto;
    private UserInfoDto userInfo;

    private Order order1;
    private Order order2;
    private OrderDto orderDto1;
    private OrderDto orderDto2;
    private UserInfoDto userInfo1;
    private UserInfoDto userInfo2;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setUserId(100L);
        order.setItems(new ArrayList<>());
        order.setStatus("PENDING");

        orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setUserId(100L);

        userInfo = new UserInfoDto();
        userInfo.setId(100L);
        userInfo.setName("Test User");

        order1 = new Order();
        order1.setId(1L);
        order1.setUserId(100L);
        order1.setStatus("PENDING");

        order2 = new Order();
        order2.setId(2L);
        order2.setUserId(200L);
        order2.setStatus("COMPLETED");

        orderDto1 = new OrderDto();
        orderDto1.setId(1L);
        orderDto1.setUserId(100L);

        orderDto2 = new OrderDto();
        orderDto2.setId(2L);
        orderDto2.setUserId(200L);

        userInfo1 = new UserInfoDto();
        userInfo1.setId(100L);
        userInfo1.setName("User1");

        userInfo2 = new UserInfoDto();
        userInfo2.setId(200L);
        userInfo2.setName("User2");
    }

    @Test
    void getOrderTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userIntegration.getUser(100L)).thenReturn(userInfo);

        OrderWithUserDto result = orderService.getOrder(1L);

        assertNotNull(result);
        assertEquals(orderDto, result.getOrder());
        assertEquals(userInfo, result.getUser());
    }

    @Test
    void getOrderOrderNotFoundTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrder(1L)
        );

        assertEquals("Order with id:1 not found", ex.getMessage());
        verify(orderRepository).findById(1L);
        verifyNoInteractions(orderMapper, userIntegration);
    }

    @Test
    void createOrderTest() {

        OrderDto request = new OrderDto();
        request.setUserId(100L);

        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userIntegration.getUser(100L)).thenReturn(userInfo);

        OrderWithUserDto result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals(orderDto, result.getOrder());
        assertEquals(userInfo, result.getUser());
    }

    @Test
    void getOrdersByStatusTest() {
        List<String> statuses = List.of("PENDING", "COMPLETED");

        when(orderRepository.findByStatusIn(statuses)).thenReturn(List.of(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toDto(order2)).thenReturn(orderDto2);
        when(userIntegration.getUser(100L)).thenReturn(userInfo1);
        when(userIntegration.getUser(200L)).thenReturn(userInfo2);

        List<OrderWithUserDto> result = orderService.getOrdersByStatus(statuses);

        assertEquals(2, result.size());
        assertEquals(orderDto1, result.get(0).getOrder());
        assertEquals(userInfo1, result.get(0).getUser());
        assertEquals(orderDto2, result.get(1).getOrder());
        assertEquals(userInfo2, result.get(1).getUser());
    }

    @Test
    void getOrdersByStatusNoOrdersFoundTest() {
        List<String> statuses = List.of("PENDING");
        when(orderRepository.findByStatusIn(statuses)).thenReturn(List.of());

        List<OrderWithUserDto> result = orderService.getOrdersByStatus(statuses);

        assertTrue(result.isEmpty());
        verifyNoInteractions(orderMapper, userIntegration);
    }

    @Test
    void updateOrderTest() {
        OrderDto request = new OrderDto();
        request.setId(1L);
        request.setUserId(100L);
        request.setItems(new ArrayList<>());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        //when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userIntegration.getUser(100L)).thenReturn(userInfo);

        OrderWithUserDto result = orderService.updateOrder(1L, request);

        assertEquals(orderDto, result.getOrder());
        assertEquals(userInfo, result.getUser());
    }

    @Test
    void updateOrderOrderNotFoundTest() {
        OrderDto request = new OrderDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrder(1L, request)
        );

        assertEquals("Order with id:1 not found", ex.getMessage());
        verifyNoInteractions(orderMapper, userIntegration);
    }

    @Test
    void getOrdersByIdsTest() {
        List<Long> ids = List.of(1L, 2L);

        when(orderRepository.findAllById(ids)).thenReturn(List.of(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toDto(order2)).thenReturn(orderDto2);
        when(userIntegration.getUser(100L)).thenReturn(userInfo1);
        when(userIntegration.getUser(200L)).thenReturn(userInfo2);

        List<OrderWithUserDto> result = orderService.getOrdersByIds(ids);

        assertEquals(2, result.size());
    }

    @Test
    void getOrdersByIdsNoOrdersFoundTest() {
        List<Long> ids = List.of(1L, 2L);
        when(orderRepository.findAllById(ids)).thenReturn(List.of());

        List<OrderWithUserDto> result = orderService.getOrdersByIds(ids);

        assertTrue(result.isEmpty());
        verifyNoInteractions(orderMapper, userIntegration);
    }

    @Test
    void deleteOrderTest() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.deleteOrder(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteOrderOrderNotFoundTest() {
        when(orderRepository.existsById(1L)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.deleteOrder(1L)
        );

        assertEquals("Order with id:1 not found", ex.getMessage());
        verify(orderRepository, never()).deleteById(anyLong());
    }
}
