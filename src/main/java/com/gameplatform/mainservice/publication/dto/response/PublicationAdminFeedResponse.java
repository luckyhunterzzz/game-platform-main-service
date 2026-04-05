package com.gameplatform.mainservice.publication.dto.response;

import java.util.List;

public record PublicationAdminFeedResponse(
        List<PublicationAdminSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
