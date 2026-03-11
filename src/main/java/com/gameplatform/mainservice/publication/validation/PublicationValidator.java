package com.gameplatform.mainservice.publication.validation;

import com.gameplatform.mainservice.exception.BusinessValidationException;
import com.gameplatform.mainservice.publication.dto.request.CreatePublicationRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class PublicationValidator {

    public void validateCreate(CreatePublicationRequest request, OffsetDateTime now) {
        validateImageFields(request);

        switch(request.status()) {
            case DRAFT -> validateDraft(request);
            case SCHEDULED -> validateScheduled(request, now);
            case PUBLISHED -> validatePublished(request, now);
            case ARCHIVED -> throw new BusinessValidationException("Publication cannot be created with ARCHIVED status");
        }
    }

    private void validateDraft(CreatePublicationRequest request) {
        if (request.publishedAt() != null) {
            throw new BusinessValidationException("publishedAt must be null for DRAFT status");
        }
    }

    private void validateScheduled(CreatePublicationRequest request, OffsetDateTime now) {
        if (request.publishedAt() == null) {
            throw new BusinessValidationException("publishedAt is required for SCHEDULED status");
        }
        if (!request.publishedAt().isAfter(now)) {
            throw new BusinessValidationException("publishedAt must be in the future for SCHEDULED status");
        }
    }

    private void validatePublished(CreatePublicationRequest request, OffsetDateTime now) {
        if (request.publishedAt() != null && request.publishedAt().isAfter(now)) {
            throw new BusinessValidationException("Future publishedAt requires SCHEDULED status");
        }
    }

    private void validateImageFields(CreatePublicationRequest request) {
        boolean bucketProvided = request.imageBucket() != null && !request.imageBucket().isBlank();
        boolean objectProvided = request.imageObjectKey() != null && !request.imageObjectKey().isBlank();

        if (bucketProvided != objectProvided) {
            throw new BusinessValidationException("imageBucket and imageObjectKey must be provided together or both be null");
        }
    }
}
