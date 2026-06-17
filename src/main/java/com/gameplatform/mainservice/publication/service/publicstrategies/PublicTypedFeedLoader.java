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
public class PublicTypedFeedLoader implements PublicPublicationFeedLoader {

    private final PublicationRepository publicationRepository;

    @Override
    public boolean supports(PublicPublicationFeedQuery query) {
        return !query.allianceOnly()
                && query.normalizedSearch() == null
                && query.type() != null
                && query.type() != PublicationType.NEWS;
    }

    @Override
    public Page<Publication> load(PublicPublicationFeedQuery query, OffsetDateTime now) {
        return publicationRepository.findPublishedForPublicFeedByType(
                PublicationStatus.PUBLISHED,
                query.type(),
                now,
                PageRequest.of(query.page(), query.size())
        );
    }
}
