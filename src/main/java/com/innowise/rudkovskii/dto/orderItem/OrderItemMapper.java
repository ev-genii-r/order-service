package com.innowise.rudkovskii.dto.orderItem;

import com.innowise.rudkovskii.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "item.id", target = "itemId")
    OrderItemDto toDto(OrderItem entity);

    @Mapping(target = "item", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderItemDto dto);
}
