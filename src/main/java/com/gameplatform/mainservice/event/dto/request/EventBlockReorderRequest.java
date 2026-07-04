package com.gameplatform.mainservice.event.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record EventBlockReorderRequest(
        @NotEmpty(message = "must not be empty")
        List<@Valid Item> items
) {
    public record Item(
            @NotNull(message = "blockId must not be null")
            Long blockId,
            @NotNull(message = "position must not be null")
            @Positive(message = "position must be positive")
            Integer position
    ) {
    }
}