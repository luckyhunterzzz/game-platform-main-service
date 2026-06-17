package com.gameplatform.mainservice.publication.service;

import com.gameplatform.mainservice.config.PublicCacheEvictionService;
import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import com.gameplatform.mainservice.publication.dto.request.PublicationUpsertRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminDetailsResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminHomeResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminSummaryResponse;
import com.gameplatform.mainservice.publication.mapper.PublicationResponseConverter;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import com.gameplatform.mainservice.publication.service.adminstrategies.AdminPublicationFeedLoader;
import com.gameplatform.mainservice.publication.validation.PublicationValidator;
import com.gameplatform.mainservice.security.CurrentUserProvider;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicationAdminService {

    private static final UUID SYSTEM_ACTOR_ID = new UUID(0L, 0L);

    private final PublicationRepository publicationRepository;
    private final PublicationResponseConverter publicationResponseConverter;
    private final PublicationValidator publicationValidator;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;
    private final PublicCacheEvictionService publicCacheEvictionService;
    private final List<AdminPublicationFeedLoader> adminPublicationFeedLoaders;

    @Value("${app.publications.page-size-default:10}")
    private int defaultPageSize;

    @Value("${app.publications.page-size-max:50}")
    private int maxPageSize;

    public PublicationAdminFeedResponse getFeedByStatus(PublicationStatus status,
                                                        String search,
                                                        PublicationType type,
                                                        int page,
                                                        Integer size) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizePageSize(size, defaultPageSize, maxPageSize);

        Page<Publication> publicationPage = loadAdminFeed(status, normalizedSearch, type, normalizedPage, normalizedSize);
        List<PublicationAdminSummaryResponse> items =
                publicationResponseConverter.toAdminSummaryResponseList(publicationPage.getContent());

        return new PublicationAdminFeedResponse(
                items,
                publicationPage.getNumber(),
                publicationPage.getSize(),
                publicationPage.getTotalElements(),
                publicationPage.getTotalPages(),
                publicationPage.hasNext()
        );
    }

    public PublicationAdminHomeResponse getHomeOverview(Integer size) {
        int normalizedSize = normalizePageSize(size, defaultPageSize, maxPageSize);

        return new PublicationAdminHomeResponse(
                getFeedByStatus(PublicationStatus.PUBLISHED, null,null, 0, normalizedSize),
                getFeedByStatus(PublicationStatus.DRAFT, null, null, 0, normalizedSize),
                getFeedByStatus(PublicationStatus.SCHEDULED, null, null, 0, normalizedSize),
                getFeedByStatus(PublicationStatus.PUBLISHED, null, PublicationType.ALLIANCE, 0, normalizedSize)
        );
    }

    public PublicationAdminDetailsResponse getById(UUID id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Publication not found: " + id));

        return publicationResponseConverter.toAdminDetailsResponse(publication);
    }

    @Transactional
    public PublicationAdminDetailsResponse create(PublicationUpsertRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        publicationValidator.validateUpsert(request, now);

        UUID authorId = currentUserProvider.getUserId();

        Publication publication = Publication.builder()
                .id(UUID.randomUUID())
                .createdBy(authorId)
                .createdAt(now)
                .build();

        applyUpsert(publication, request, now, authorId);

        Publication savedPublication = publicationRepository.save(publication);
        publicCacheEvictionService.evictPublicationCaches();

        return publicationResponseConverter.toAdminDetailsResponse(savedPublication);
    }

    @Transactional
    public PublicationAdminDetailsResponse update(UUID id, PublicationUpsertRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        publicationValidator.validateUpsert(request, now);

        UUID authorId = currentUserProvider.getUserId();

        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Publication not found: " + id));

        applyUpsert(publication, request, now, authorId);

        Publication savedPublication = publicationRepository.save(publication);
        publicCacheEvictionService.evictPublicationCaches();
        return publicationResponseConverter.toAdminDetailsResponse(savedPublication);
    }

    @Transactional
    public int publishScheduledPublications() {
        OffsetDateTime now = OffsetDateTime.now(clock);

        List<Publication> publicationsToPublish = publicationRepository
                .findAllByStatusAndPublishedAtLessThanEqual(PublicationStatus.SCHEDULED, now);

        for (Publication publication : publicationsToPublish) {
            publication.setStatus(PublicationStatus.PUBLISHED);
            publication.setUpdatedAt(now);
            publication.setUpdatedBy(SYSTEM_ACTOR_ID);

            if (publication.isPinned() && publication.getPinnedAt() == null) {
                publication.setPinnedAt(now);
            }
        }

        if (!publicationsToPublish.isEmpty()) {
            publicCacheEvictionService.evictPublicationCaches();
        }

        return publicationsToPublish.size();
    }

    @Transactional
    public int unpinExpiredPublications() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        List<Publication> publicationsToUnpin = publicationRepository
                .findAllByPinnedTrueAndPinnedUntilLessThanEqual(now);

        for (Publication publication : publicationsToUnpin) {
            publication.setPinned(false);
            publication.setPinnedAt(null);
            publication.setPinnedUntil(null);
            publication.setUpdatedAt(now);
            publication.setUpdatedBy(SYSTEM_ACTOR_ID);
        }

        if (!publicationsToUnpin.isEmpty()) {
            publicCacheEvictionService.evictPublicationCaches();
        }

        return publicationsToUnpin.size();
    }

    private void applyUpsert(Publication publication,
                             PublicationUpsertRequest request,
                             OffsetDateTime now,
                             UUID actorId) {
        publication.setType(request.type());
        publication.setTitleJson(request.titleJson());
        publication.setContentJson(request.contentJson());
        publication.setImageBucket(request.imageBucket());
        publication.setImageObjectKey(request.imageObjectKey());
        publication.setStatus(request.status());
        publication.setPublishedAt(resolvePublishedAt(request, now));
        publication.setPinned(request.pinned());
        publication.setPinnedUntil(request.pinned() ? request.pinnedUntil() : null);
        publication.setShowInNewsFeed(request.showInNewsFeed());
        publication.setPinnedAt(resolvePinnedAt(request, now));
        publication.setUpdatedBy(actorId);
        publication.setUpdatedAt(now);
    }

    private OffsetDateTime resolvePublishedAt(PublicationUpsertRequest request, OffsetDateTime now) {
        return switch (request.status()) {
            case DRAFT, ARCHIVED -> null;
            case PUBLISHED -> request.publishedAt() != null ? request.publishedAt() : now;
            case SCHEDULED -> request.publishedAt();
        };
    }

    private OffsetDateTime resolvePinnedAt(PublicationUpsertRequest request, OffsetDateTime now) {
        if (!request.pinned()) {
            return null;
        }

        return switch (request.status()) {
            case PUBLISHED -> now;
            case DRAFT, SCHEDULED, ARCHIVED -> null;
        };
    }

    private Page<Publication> loadAdminFeed(PublicationStatus status,
                                            String normalizedSearch,
                                            PublicationType type, int page, int size) {
        AdminPublicationFeedLoader loader = adminPublicationFeedLoaders.stream()
                .filter(candidate -> candidate.supports(status))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No admin publication feed loader for status " + status));

        return loader.load(status, normalizedSearch, type, page, size);
    }

    private int normalizePageSize(Integer size, int defaultSize, int maxSize) {
        return Math.min(Math.max(size != null ? size : defaultSize, 1), maxSize);
    }
}
