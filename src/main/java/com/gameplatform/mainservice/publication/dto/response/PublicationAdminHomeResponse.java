package com.gameplatform.mainservice.publication.dto.response;

public record PublicationAdminHomeResponse(
        PublicationAdminFeedResponse published,
        PublicationAdminFeedResponse drafts,
        PublicationAdminFeedResponse scheduled,
        PublicationAdminFeedResponse alliances
) {
}
