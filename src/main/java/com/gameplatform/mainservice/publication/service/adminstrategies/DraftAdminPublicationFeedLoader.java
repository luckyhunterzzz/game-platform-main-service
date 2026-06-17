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
public class DraftAdminPublicationFeedLoader implements AdminPublicationFeedLoader {

    private static final String UPDATED_AT_FIELD = "updatedAt";
    private static final String CREATED_AT_FIELD = "createdAt";

    private final PublicationRepository publicationRepository;

    @Override
    public boolean supports(PublicationStatus status) {
        return status == PublicationStatus.DRAFT || status == PublicationStatus.ARCHIVED;
    }

    @Override
    public Page<Publication> load(PublicationStatus status,
                                  String normalizedSearch,
                                  PublicationType type,
                                  int page,
                                  int size) {
        if (normalizedSearch == null) {
            PageRequest pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Order.desc(UPDATED_AT_FIELD),
                    Sort.Order.desc(CREATED_AT_FIELD)
            ));

            return type == null
                    ? publicationRepository.findAllByStatus(status, pageable)
                    : publicationRepository.findAllByStatusAndType(status, type, pageable);
        }

        return publicationRepository.searchDraftsByTitle(
                status.name(),
                type != null ? type.name() : null,
                normalizedSearch,
                PageRequest.of(page, size)
        );
    }
}
