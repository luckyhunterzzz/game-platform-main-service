package com.gameplatform.mainservice.event.dto.response;

import java.util.List;

public record EventFeedResponse(
        List<EventSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
