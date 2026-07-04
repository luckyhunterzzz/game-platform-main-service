package com.gameplatform.mainservice.event.dto.response;

public record EventSummaryResponse(
        Long id,
        String slug,
        String name,
        String description,
        String imageUrl
) {
}
