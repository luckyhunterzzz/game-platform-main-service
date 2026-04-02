package com.gameplatform.mainservice.publication.service;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.dto.request.CreatePublicationRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.mapper.PublicationResponseConverter;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import com.gameplatform.mainservice.publication.validation.PublicationValidator;
import com.gameplatform.mainservice.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
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
    private static final int MAX_PAGE_SIZE = 50;
    private static final String PINNED_FIELD = "pinned";
    private static final String PINNED_AT_FIELD = "pinnedAt";
    private static final String PUBLISHED_AT_FIELD = "publishedAt";
    private static final String UPDATED_AT_FIELD = "updatedAt";
    private static final String CREATED_AT_FIELD = "createdAt";

    private final PublicationRepository publicationRepository;
    private final PublicationResponseConverter publicationResponseConverter;
    private final PublicationValidator publicationValidator;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PublicationFeedResponse getFeedByStatus(PublicationStatus status, int page, Integer size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size != null ? size : DEFAULT_PAGE_SIZE, 1), MAX_PAGE_SIZE);

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedSize, resolveSort(status));
        Page<Publication> publicationPage = publicationRepository.findAllByStatus(status, pageable);
        List<PublicationResponse> items = publicationResponseConverter.toResponseList(publicationPage.getContent());

        return new PublicationFeedResponse(
                items,
                publicationPage.getNumber(),
                publicationPage.getSize(),
                publicationPage.getTotalElements(),
                publicationPage.getTotalPages(),
                publicationPage.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public PublicationResponse getById(UUID id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found: " + id));

        return publicationResponseConverter.toResponse(publication);
    }

    @Transactional
    public PublicationResponse create(CreatePublicationRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        publicationValidator.validateCreate(request, now);

        UUID authorId = currentUserProvider.getUserId();

        Publication publication = buildPublicationEntity(request, now, authorId);

        Publication savedPublication = publicationRepository.save(publication);

        return publicationResponseConverter.toResponse(savedPublication);
    }

    private Publication buildPublicationEntity(CreatePublicationRequest request,
                                               OffsetDateTime now,
                                               UUID authorId) {
        return Publication.builder()
                .id(UUID.randomUUID())
                .type(request.type())
                .title(request.title())
                .content(request.content())
                .imageBucket(request.imageBucket())
                .imageObjectKey(request.imageObjectKey())
                .status(request.status())
                .publishedAt(resolvePublishedAt(request, now))
                .pinned(request.pinned())
                .pinnedAt(resolvePinnedAt(request, now))
                .createdBy(authorId)
                .updatedBy(authorId)
                .createdAt(now)
                .updatedAt(now)
                .build();

    }

    private OffsetDateTime resolvePublishedAt(CreatePublicationRequest request, OffsetDateTime now) {
        return switch (request.status()) {
            case DRAFT, ARCHIVED -> null;
            case PUBLISHED -> request.publishedAt() != null ? request.publishedAt() : now;
            case SCHEDULED -> request.publishedAt();
        };
    }

    private OffsetDateTime resolvePinnedAt(CreatePublicationRequest request, OffsetDateTime now) {
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
}
