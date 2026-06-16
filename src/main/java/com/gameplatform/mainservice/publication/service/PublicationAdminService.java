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
import com.gameplatform.mainservice.publication.validation.PublicationValidator;
import com.gameplatform.mainservice.security.CurrentUserProvider;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicationAdminService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_HOME_SECTION_SIZE = 5;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_HOME_SECTION_SIZE = 20;
    private static final UUID SYSTEM_ACTOR_ID = new UUID(0L, 0L);
    private static final String PINNED_FIELD = "pinned";
    private static final String PINNED_AT_FIELD = "pinnedAt";
    private static final String PINNED_UNTIL_FIELD = "pinnedUntil";
    private static final String PUBLISHED_AT_FIELD = "publishedAt";
    private static final String UPDATED_AT_FIELD = "updatedAt";
    private static final String CREATED_AT_FIELD = "createdAt";

    private final PublicationRepository publicationRepository;
    private final PublicationResponseConverter publicationResponseConverter;
    private final PublicationValidator publicationValidator;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;
    private final PublicCacheEvictionService publicCacheEvictionService;

    @Transactional(readOnly = true)
    public PublicationAdminFeedResponse getFeedByStatus(PublicationStatus status,
                                                        PublicationType type,
                                                        int page,
                                                        Integer size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizePageSize(size, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedSize, resolveSort(status));
        Page<Publication> publicationPage = type == null
                ? publicationRepository.findAllByStatus(status, pageable)
                : publicationRepository.findAllByStatusAndType(status, type, pageable);
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

    @Transactional(readOnly = true)
    public PublicationAdminHomeResponse getHomeOverview(Integer size) {
        int normalizedSize = normalizePageSize(size, DEFAULT_HOME_SECTION_SIZE, MAX_HOME_SECTION_SIZE);

        return new PublicationAdminHomeResponse(
                getFeedByStatus(PublicationStatus.PUBLISHED, null, 0, normalizedSize),
                getFeedByStatus(PublicationStatus.DRAFT, null, 0, normalizedSize),
                getFeedByStatus(PublicationStatus.SCHEDULED, null, 0, normalizedSize),
                getFeedByStatus(PublicationStatus.PUBLISHED, PublicationType.ALLIANCE, 0, normalizedSize)
        );
    }

    @Transactional(readOnly = true)
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

    private Sort resolveSort(PublicationStatus status) {
        return switch (status) {
            case PUBLISHED -> Sort.by(
                    Sort.Order.desc(PINNED_FIELD),
                    Sort.Order.asc(PINNED_UNTIL_FIELD),
                    Sort.Order.desc(PINNED_AT_FIELD),
                    Sort.Order.desc(PUBLISHED_AT_FIELD),
                    Sort.Order.desc(CREATED_AT_FIELD)
            );
            case DRAFT, ARCHIVED -> Sort.by(
                    Sort.Order.desc(UPDATED_AT_FIELD),
                    Sort.Order.desc(CREATED_AT_FIELD)
            );
            case SCHEDULED -> Sort.by(
                    Sort.Order.asc(PUBLISHED_AT_FIELD),
                    Sort.Order.desc(CREATED_AT_FIELD)
            );
        };
    }

    private int normalizePageSize(Integer size, int defaultSize, int maxSize) {
        return Math.min(Math.max(size != null ? size : defaultSize, 1), maxSize);
    }
}

