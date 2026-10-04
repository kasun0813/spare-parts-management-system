package com.autohublanka.sales.dto;

import jakarta.validation.constraints.NotNull;

public record WishlistRequest(
        @NotNull(message = "Spare part is required")
        Long sparePartId
) {
}
