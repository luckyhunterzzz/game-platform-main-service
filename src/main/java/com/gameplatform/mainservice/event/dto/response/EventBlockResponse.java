package com.gameplatform.mainservice.event.dto.response;

public record EventBlockResponse(
        Long id,
        int position,
        String name,
        String description,
        String imageUrl
) {
}
