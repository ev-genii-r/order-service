package com.innowise.rudkovskii.repository;

import com.innowise.rudkovskii.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusIn(List<String> statuses);
}
