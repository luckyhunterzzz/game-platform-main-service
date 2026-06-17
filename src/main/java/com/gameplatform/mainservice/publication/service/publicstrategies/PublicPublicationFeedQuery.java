package com.gameplatform.mainservice.publication.service.publicstrategies;

import com.gameplatform.mainservice.publication.domain.enums.PublicationType;

public record PublicPublicationFeedQuery(
        int page,
        int size,
        String normalizedSearch,
        PublicationType type,
        boolean allianceOnly
) {
}
