package com.gameplatform.mainservice.event.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EventBlockReorderRequest(
        @NotEmpty(message = "must not be empty")
        List<Long> blockIds
) {
}
