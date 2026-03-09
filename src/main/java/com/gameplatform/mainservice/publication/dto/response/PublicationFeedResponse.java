package com.gameplatform.mainservice.publication.dto.response;

import java.util.List;

public record PublicationFeedResponse(
        List<PublicationResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}