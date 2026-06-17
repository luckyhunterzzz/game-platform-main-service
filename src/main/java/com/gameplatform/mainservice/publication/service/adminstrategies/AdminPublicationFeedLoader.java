package com.gameplatform.mainservice.publication.service.adminstrategies;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import org.springframework.data.domain.Page;

public interface AdminPublicationFeedLoader {

    boolean supports(PublicationStatus status);

    Page<Publication> load(PublicationStatus status,
                           String normalizedSearch,
                           PublicationType type,
                           int page,
                           int size);
}
