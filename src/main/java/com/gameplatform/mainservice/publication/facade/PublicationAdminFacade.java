package com.gameplatform.mainservice.publication.facade;

import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.dto.request.PublicationUpsertRequest;
import com.gameplatform.mainservice.publication.dto.response.PublicationFeedResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.service.PublicationAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PublicationAdminFacade {

    private final PublicationAdminService publicationAdminService;

    public PublicationResponse getPublicationById(UUID id) {
        return publicationAdminService.getById(id);
    }

    public PublicationFeedResponse getFeedByStatus(PublicationStatus status, int page, Integer size) {
        return publicationAdminService.getFeedByStatus(status, page, size);
    }

    public PublicationResponse createPublication(PublicationUpsertRequest publicationUpsertRequest) {
        return publicationAdminService.create(publicationUpsertRequest);
    }

    public PublicationResponse updatePublication(UUID id, PublicationUpsertRequest publicationUpsertRequest) {
        return publicationAdminService.update(id, publicationUpsertRequest);
    }
}
