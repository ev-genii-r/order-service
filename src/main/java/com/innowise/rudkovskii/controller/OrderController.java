package com.innowise.rudkovskii.controller;

import com.innowise.rudkovskii.dto.order.OrderDto;
import com.innowise.rudkovskii.dto.order.OrderWithUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.innowise.rudkovskii.service.OrderService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderWithUserDto create(@Valid @RequestBody OrderDto request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderWithUserDto get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PostMapping("/batch")
    public List<OrderWithUserDto> getByIds(@RequestBody List<Long> ids) {
        return orderService.getOrdersByIds(ids);
    }

    @PostMapping("/statuses")
    public List<OrderWithUserDto> getByStatuses(@RequestBody List<String> statuses) {
        return orderService.getOrdersByStatus(statuses);
    }

    @PutMapping("/{id}")
    public OrderWithUserDto update(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto request
    ) {
        return orderService.updateOrder(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }
}

