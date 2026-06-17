package com.gameplatform.mainservice.publication.service.publicstrategies;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;

public interface PublicPublicationFeedLoader {

    boolean supports(PublicPublicationFeedQuery query);

    Page<Publication> load(PublicPublicationFeedQuery query, OffsetDateTime now);
}
