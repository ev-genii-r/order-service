package com.innowise.rudkovskii.repository;

import com.innowise.rudkovskii.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
