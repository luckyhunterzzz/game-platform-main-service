package com.gameplatform.mainservice.publication.validation;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.publication.dto.request.PublicationUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class PublicationValidator {

    private static final String ARCHIVED_NOT_SUPPORTED_MESSAGE =
            "Publication cannot be saved with ARCHIVED status";
    private static final int SCHEDULE_MINUTE_STEP = 15;

    private final ImageReferenceValidator imageReferenceValidator;

    public void validateUpsert(PublicationUpsertRequest request, OffsetDateTime now) {
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());
        validateLocalizedFields(request);
        validateStatusRules(request, now);
    }

    private void validateLocalizedFields(PublicationUpsertRequest request) {
        if (!hasAnyText(request.titleJson())) {
            throw new BusinessValidationException("titleJson must contain at least one localized value");
        }
    }

    private void validateStatusRules(PublicationUpsertRequest request, OffsetDateTime now) {
        validatePinRules(request, now);

        switch (request.status()) {
            case DRAFT -> validateDraft(request);
            case SCHEDULED -> validateScheduled(request, now);
            case PUBLISHED -> validatePublished(request, now);
            case ARCHIVED -> throw new BusinessValidationException(ARCHIVED_NOT_SUPPORTED_MESSAGE);
        }
    }

    private void validatePinRules(PublicationUpsertRequest request, OffsetDateTime now) {
        if (request.pinnedUntil() != null && !request.pinned()) {
            throw new BusinessValidationException("pinnedUntil requires pinned=true");
        }

        if (request.pinnedUntil() != null && !request.pinnedUntil().isAfter(now)) {
            throw new BusinessValidationException("pinnedUntil must be in the future");
        }
    }

    private void validateDraft(PublicationUpsertRequest request) {
        if (request.publishedAt() != null) {
            throw new BusinessValidationException("publishedAt must be null for DRAFT status");
        }
    }

    private void validateScheduled(PublicationUpsertRequest request, OffsetDateTime now) {
        if (request.publishedAt() == null) {
            throw new BusinessValidationException("publishedAt is required for SCHEDULED status");
        }
        if (!request.publishedAt().isAfter(now)) {
            throw new BusinessValidationException("publishedAt must be in the future for SCHEDULED status");
        }
        if (!isQuarterHourSlot(request.publishedAt())) {
            throw new BusinessValidationException("publishedAt must use 15-minute steps for SCHEDULED status");
        }
    }

    private void validatePublished(PublicationUpsertRequest request, OffsetDateTime now) {
        if (request.publishedAt() != null && request.publishedAt().isAfter(now)) {
            throw new BusinessValidationException("Future publishedAt requires SCHEDULED status");
        }
    }

    private boolean isQuarterHourSlot(OffsetDateTime publishedAt) {
        return publishedAt.getMinute() % SCHEDULE_MINUTE_STEP == 0
                && publishedAt.getSecond() == 0
                && publishedAt.getNano() == 0;
    }

    private boolean hasAnyText(LocalizedTextJson json) {
        if (json == null) {
            return false;
        }

        return hasText(json.ru()) || hasText(json.en());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
