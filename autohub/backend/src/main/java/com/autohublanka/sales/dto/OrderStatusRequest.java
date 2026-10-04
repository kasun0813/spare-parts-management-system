package com.autohublanka.sales.dto;

import com.autohublanka.sales.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
