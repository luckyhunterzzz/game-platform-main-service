package com.gameplatform.mainservice.publication.facade;

import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.dto.request.CreatePublicationRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.service.PublicationAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicationAdminFacade {

    private final PublicationAdminService publicationAdminService;

    public PublicationFeedResponse getFeedByStatus(PublicationStatus status, int page, Integer size) {
        return publicationAdminService.getFeedByStatus(status, page, size);
    }

    public PublicationResponse createPublication(CreatePublicationRequest createPublicationRequest) {
        return publicationAdminService.create(createPublicationRequest);
    }
}
