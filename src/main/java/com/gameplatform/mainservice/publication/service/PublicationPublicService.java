package com.gameplatform.mainservice.publication.service;

import com.gameplatform.mainservice.config.CacheNames;
import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationLanguage;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.mapper.PublicationResponseConverter;
import com.gameplatform.mainservice.publication.service.publicstrategies.PublicPublicationFeedLoader;
import com.gameplatform.mainservice.publication.service.publicstrategies.PublicPublicationFeedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicationPublicService {

    private final PublicationResponseConverter publicationResponseConverter;
    private final Clock clock;
    private final List<PublicPublicationFeedLoader> publicPublicationFeedLoaders;

    @Value("${app.publications.page-size-default:10}")
    private int defaultPageSize;

    @Value("${app.publications.page-size-max:50}")
    private int maxPageSize;

    @Cacheable(cacheNames = CacheNames.PUBLIC_PUBLICATIONS_FEED)
    public PublicationFeedResponse getLatestPublicFeed(int page,
                                                       Integer size,
                                                       PublicationLanguage language,
                                                       String search,
                                                       PublicationType type) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        int normalizedPage = Math.max(page, 0);
        int normalizedPageSize = normalizePageSize(size);

        Page<Publication> publicationPage = loadPublicFeed(new PublicPublicationFeedQuery(
                normalizedPage,
                normalizedPageSize,
                normalizedSearch,
                type,
                false
        ));

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
        int normalizedPage = Math.max(page, 0);
        int normalizedPageSize = normalizePageSize(size);

        Page<Publication> publicationPage = loadPublicFeed(new PublicPublicationFeedQuery(
                normalizedPage,
                normalizedPageSize,
                null,
                PublicationType.ALLIANCE,
                true
        ));

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

    private int normalizePageSize(Integer size) {
        int requestedSize = size != null ? size : defaultPageSize;
        return Math.min(Math.max(requestedSize, 1), maxPageSize);
    }

    private Page<Publication> loadPublicFeed(PublicPublicationFeedQuery query) {
        OffsetDateTime now = OffsetDateTime.now(clock);

        PublicPublicationFeedLoader loader = publicPublicationFeedLoaders.stream()
                .filter(candidate -> candidate.supports(query))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No public publication feed loader for query " + query));

        return loader.load(query, now);
    }
}
