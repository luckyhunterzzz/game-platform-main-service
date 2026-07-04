package com.gameplatform.mainservice.event.dto.response;

import java.util.List;

public record EventResponse(
        Long id,
        String slug,
        String name,
        String description,
        String imageUrl,
        List<EventBlockResponse> blocks
) {
}
