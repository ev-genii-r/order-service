package com.innowise.rudkovskii.service;

import com.innowise.rudkovskii.dto.order.OrderDto;
import com.innowise.rudkovskii.dto.order.OrderMapper;
import com.innowise.rudkovskii.dto.order.OrderWithUserDto;
import com.innowise.rudkovskii.dto.orderItem.OrderItemMapper;
import com.innowise.rudkovskii.dto.user.UserInfoDto;
import com.innowise.rudkovskii.entity.Order;
import com.innowise.rudkovskii.entity.OrderItem;
import com.innowise.rudkovskii.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.innowise.rudkovskii.repository.ItemRepository;
import com.innowise.rudkovskii.repository.OrderRepository;

import java.util.List;
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserIntegrationService userIntegration;

    public OrderWithUserDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id:" + orderId));

        OrderDto orderDto = orderMapper.toDto(order);
        UserInfoDto user = userIntegration.getUser(order.getUserId());

        return new OrderWithUserDto(orderDto, user);
    }

    public OrderWithUserDto createOrder(OrderDto request) {
        Order order = orderMapper.toEntity(request);
        order = orderRepository.save(order);

        OrderDto orderDto = orderMapper.toDto(order);

        UserInfoDto user = userIntegration.getUser(request.getUserId());

        return new OrderWithUserDto(orderDto, user);
    }

    public List<OrderWithUserDto> getOrdersByStatus(List<String> statuses) {

        return orderRepository.findByStatusIn(statuses).stream()
                .map(order -> {
                    var orderDto = orderMapper.toDto(order);
                    var user = userIntegration.getUser(order.getUserId());
                    return new OrderWithUserDto(orderDto, user);
                })
                .toList();
    }

    @Transactional
    public OrderWithUserDto updateOrder(Long id, OrderDto request) {

        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id:" + id));

        existingOrder = orderMapper.toEntity(request);

        Order updatedOrder = orderRepository.save(existingOrder);

        OrderDto updatedOrderDto = orderMapper.toDto(updatedOrder);
        UserInfoDto user = userIntegration.getUser(updatedOrder.getUserId());

        return new OrderWithUserDto(updatedOrderDto, user);
    }

    public List<OrderWithUserDto> getOrdersByIds(List<Long> ids) {
        return orderRepository.findAllById(ids).stream()
                .map(order -> {
                    OrderDto dto = orderMapper.toDto(order);
                    UserInfoDto user = userIntegration.getUser(order.getUserId());
                    return new OrderWithUserDto(dto, user);
                })
                .toList();
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if(!orderRepository.existsById(orderId)){
            throw new ResourceNotFoundException("Order with id:" + orderId);
        }
        orderRepository.deleteById(orderId);
    }
}



