package com.gameplatform.mainservice.publication.service;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.dto.request.CreatePublicationRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.mapper.PublicationResponseConverter;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import com.gameplatform.mainservice.publication.validation.PublicationValidator;
import com.gameplatform.mainservice.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicationAdminService {

    private final PublicationRepository publicationRepository;
    private final PublicationResponseConverter publicationResponseConverter;
    private final PublicationValidator publicationValidator;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

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
}
