package com.gameplatform.mainservice.event.validation;

import com.gameplatform.mainservice.event.domain.entity.Event;
import com.gameplatform.mainservice.event.dto.request.EventBlockReorderRequest;
import com.gameplatform.mainservice.event.dto.request.EventBlockUpsertRequest;
import com.gameplatform.mainservice.event.dto.request.EventUpsertRequest;
import com.gameplatform.mainservice.event.repository.EventRepository;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class EventValidator {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]+$");

    private final ImageReferenceValidator imageReferenceValidator;
    private final EventRepository eventRepository;

    public void validateEventUpsert(EventUpsertRequest request, Long eventId) {
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());
        validateLocalizedText(request.nameJson(), "nameJson");
        validateSlug(request.slug(), eventId);
    }

    public void validateBlockUpsert(EventBlockUpsertRequest request) {
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());
        validateLocalizedText(request.nameJson(), "nameJson");
    }

    public void validateBlockReorder(Event event, EventBlockReorderRequest request) {
        List<Long> requestedIds = request.blockIds();
        if (requestedIds == null || requestedIds.isEmpty()) {
            throw new BusinessValidationException("blockIds must not be empty");
        }

        if (requestedIds.stream().anyMatch(id -> id == null)) {
            throw new BusinessValidationException("blockIds must not contain null values");
        }

        if (requestedIds.size() != new HashSet<>(requestedIds).size()) {
            throw new BusinessValidationException("blockIds must not contain duplicates");
        }

        Set<Long> actualIds = event.getBlocks().stream()
                .map(block -> block.getId())
                .collect(java.util.stream.Collectors.toSet());

        if (actualIds.size() != requestedIds.size() || !actualIds.containsAll(requestedIds)) {
            throw new BusinessValidationException("blockIds must contain every block of the event exactly once");
        }
    }

    private void validateSlug(String slug, Long eventId) {
        if (slug == null || slug.isBlank()) {
            throw new BusinessValidationException("slug must not be blank");
        }

        String normalizedSlug = slug.trim().toLowerCase(Locale.ROOT);

        if (!SLUG_PATTERN.matcher(normalizedSlug).matches()) {
            throw new BusinessValidationException("slug must contain only lowercase letters, numbers, and hyphens");
        }

        boolean exists = eventId == null
                ? eventRepository.existsBySlug(normalizedSlug)
                : eventRepository.existsBySlugAndIdNot(normalizedSlug, eventId);

        if (exists) {
            throw new BusinessValidationException("Event slug already exists: " + normalizedSlug);
        }
    }

    private void validateLocalizedText(LocalizedTextJson json, String fieldName) {
        if (!hasAnyText(json)) {
            throw new BusinessValidationException(fieldName + " must contain at least one localized value");
        }
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
