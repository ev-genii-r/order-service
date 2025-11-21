package com.innowise.rudkovskii.dto.item;

import com.innowise.rudkovskii.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);
    Item toEntity(ItemDto dto);
}
