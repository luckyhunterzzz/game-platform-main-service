package com.gameplatform.mainservice.publication.service.adminstrategies;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublishedAdminPublicationFeedLoader implements AdminPublicationFeedLoader {

    private static final String PINNED_FIELD = "pinned";
    private static final String PINNED_AT_FIELD = "pinnedAt";
    private static final String PUBLISHED_AT_FIELD = "publishedAt";
    private static final String CREATED_AT_FIELD = "createdAt";

    private final PublicationRepository publicationRepository;

    @Override
    public boolean supports(PublicationStatus status) {
        return status == PublicationStatus.PUBLISHED;
    }

    @Override
    public Page<Publication> load(PublicationStatus status,
                                  String normalizedSearch,
                                  PublicationType type,
                                  int page,
                                  int size) {
        if (normalizedSearch == null) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc(PINNED_FIELD),
                    Sort.Order.desc(PINNED_AT_FIELD),
                    Sort.Order.desc(PUBLISHED_AT_FIELD),
                    Sort.Order.desc(CREATED_AT_FIELD)
            ));

            return type == null
                    ? publicationRepository.findAllByStatus(status, pageable)
                    : publicationRepository.findAllByStatusAndType(status, type, pageable);
        }

        return publicationRepository.searchPublishedByTitle(
                status.name(),
                type != null ? type.name() : null,
                normalizedSearch,
                PageRequest.of(page, size)
        );
    }
}
