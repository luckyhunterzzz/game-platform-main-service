package com.gameplatform.mainservice.publication.service.publicstrategies;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class PublicSearchFeedLoader implements PublicPublicationFeedLoader {

    private final PublicationRepository publicationRepository;

    @Override
    public boolean supports(PublicPublicationFeedQuery query) {
        return !query.allianceOnly() && query.normalizedSearch() != null;
    }

    @Override
    public Page<Publication> load(PublicPublicationFeedQuery query, OffsetDateTime now) {
        if (query.type() == null) {
            return publicationRepository.searchPublishedPublicFeedByTitleExcludingType(
                    PublicationStatus.PUBLISHED.name(),
                    PublicationType.ALLIANCE.name(),
                    query.normalizedSearch(),
                    PageRequest.of(query.page(), query.size())
            );
        }

        return publicationRepository.searchPublishedPublicFeedByTypeAndTitle(
                PublicationStatus.PUBLISHED.name(),
                query.type().name(),
                query.normalizedSearch(),
                PageRequest.of(query.page(), query.size())
        );
    }
}
