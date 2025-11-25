package com.innowise.rudkovskii.repository;

import com.innowise.rudkovskii.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
