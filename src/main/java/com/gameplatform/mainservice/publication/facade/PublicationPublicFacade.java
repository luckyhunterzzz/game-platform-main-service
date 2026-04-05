package com.gameplatform.mainservice.publication.facade;

import com.gameplatform.mainservice.publication.domain.enums.PublicationLanguage;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.service.PublicationPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicationPublicFacade {

    private final PublicationPublicService publicationPublicService;

    public PublicationFeedResponse getLatestPublicFeed(int page, Integer size, PublicationLanguage language) {
        return publicationPublicService.getLatestPublicFeed(page, size, language);
    }
}
