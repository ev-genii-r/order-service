package com.innowise.rudkovskii.dto.order;

import com.innowise.rudkovskii.dto.orderItem.OrderItemMapper;
import com.innowise.rudkovskii.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    OrderDto toDto(Order order);

    @Mapping(target = "items", ignore = true)
    Order toEntity(OrderDto dto);
}

