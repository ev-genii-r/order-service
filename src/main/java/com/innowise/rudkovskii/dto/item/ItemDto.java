package com.innowise.rudkovskii.dto.item;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class ItemDto{

    @NotNull
    @Positive
    private Long id;

    @NotBlank(message = "Item name cannot be empty")
    private String name;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private Double price;
}
