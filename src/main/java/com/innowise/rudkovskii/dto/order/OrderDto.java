package com.innowise.rudkovskii.dto.order;

import com.innowise.rudkovskii.dto.orderItem.OrderItemDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderDto {

        @NotNull
        @Positive
        private Long id;

        @NotNull(message = "UserId cannot be null")
        @Positive(message = "UserId must be positive")
        private Long userId;

        @NotNull(message = "Status cannot be null")
        private String status;

        @NotNull(message = "Creation date cannot be null")
        private LocalDateTime creationDate;

        @NotEmpty(message = "Order must contain items")
        private List<OrderItemDto> items;
}

