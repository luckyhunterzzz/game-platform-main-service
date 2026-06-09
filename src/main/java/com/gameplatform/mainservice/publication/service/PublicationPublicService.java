package com.gameplatform.mainservice.publication.service;

import com.gameplatform.mainservice.config.CacheNames;
import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationLanguage;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.mapper.PublicationResponseConverter;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PublicationPublicService {

    private final PublicationRepository publicationRepository;
    private final PublicationResponseConverter publicationResponseConverter;
    private final Clock clock;
    private final int defaultPageSize;
    private final int maxPageSize;

    public PublicationPublicService(PublicationRepository publicationRepository,
                                    PublicationResponseConverter publicationResponseConverter,
                                    Clock clock,
                                    @Value("${app.publications.default-page-size:10}") int defaultPageSize,
                                    @Value("${app.publications.page-size-max:50}") int maxPageSize) {
        this.publicationRepository = publicationRepository;
        this.publicationResponseConverter = publicationResponseConverter;
        this.clock = clock;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }


    @Cacheable(cacheNames = CacheNames.PUBLIC_PUBLICATIONS_FEED)
    public PublicationFeedResponse getLatestPublicFeed(int page,
                                                       Integer size,
                                                       PublicationLanguage language,
                                                       PublicationType type) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        int normalizedPage = Math.max(page, 0);
        int normalizedPageSize = normalizePageSize(size);

        Page<Publication> publicationPage;
        if (type == null) {
            publicationPage = publicationRepository.findPublishedForPublicFeed(
                    PublicationStatus.PUBLISHED,
                    now,
                    PageRequest.of(normalizedPage, normalizedPageSize)
            );
        } else if (type == PublicationType.NEWS) {
            publicationPage = publicationRepository.findPublishedForNewsFeed(
                    PublicationStatus.PUBLISHED,
                    PublicationType.NEWS,
                    PublicationType.ALLIANCE,
                    now,
                    PageRequest.of(normalizedPage, normalizedPageSize)
            );
        } else {
            publicationPage = publicationRepository.findPublishedForPublicFeedByType(
                    PublicationStatus.PUBLISHED,
                    type,
                    now,
                    PageRequest.of(normalizedPage, normalizedPageSize)
            );
        }

        List<PublicationResponse> items = publicationResponseConverter.toPublicResponseList(
                publicationPage.getContent(),
                language
        );

        return new PublicationFeedResponse(
                items,
                publicationPage.getNumber(),
                publicationPage.getSize(),
                publicationPage.getTotalElements(),
                publicationPage.getTotalPages(),
                publicationPage.hasNext()
        );
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_PUBLICATIONS_FEED)
    public PublicationFeedResponse getAllianceFeed(int page,
                                                   Integer size,
                                                   PublicationLanguage language) {
        return getLatestPublicFeed(page, size, language, PublicationType.ALLIANCE);
    }

    private int normalizePageSize(Integer size) {
        int requestedSize = size != null ? size : defaultPageSize;
        return Math.min(Math.max(requestedSize, 1), maxPageSize);
    }
}
