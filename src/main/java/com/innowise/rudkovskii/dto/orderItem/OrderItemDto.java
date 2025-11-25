package com.innowise.rudkovskii.dto.orderItem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderItemDto{

    @NotNull
    @Positive
    private Long id;

    @NotNull
    @Positive
    private Long itemId;

    @NotNull
    @Positive
    private Integer quantity;
}
