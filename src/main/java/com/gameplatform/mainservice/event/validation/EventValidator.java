package com.gameplatform.mainservice.event.validation;

import com.gameplatform.mainservice.event.domain.entity.EventBlock;
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
import java.util.stream.Collectors;

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

    public void validateBlockReorder(List<EventBlock> actualBlocks, EventBlockReorderRequest request) {
        List<EventBlockReorderRequest.Item> items = request.items();
        if (items == null || items.isEmpty()) {
            throw new BusinessValidationException("items must not be empty");
        }

        if (items.stream().anyMatch(item -> item == null || item.blockId() == null || item.position() == null)) {
            throw new BusinessValidationException("each reorder item must contain blockId and position");
        }

        if (items.size() != new HashSet<>(items.stream().map(EventBlockReorderRequest.Item::blockId).toList()).size()) {
            throw new BusinessValidationException("blockIds must not contain duplicates");
        }

        if (items.size() != new HashSet<>(items.stream().map(EventBlockReorderRequest.Item::position).toList()).size()) {
            throw new BusinessValidationException("positions must not contain duplicates");
        }

        int expectedSize = actualBlocks.size();
        Set<Integer> requestedPositions = items.stream()
                .map(EventBlockReorderRequest.Item::position)
                .collect(Collectors.toSet());

        for (int position = 1; position <= expectedSize; position++) {
            if (!requestedPositions.contains(position)) {
                throw new BusinessValidationException("positions must contain every value from 1 to " + expectedSize);
            }
        }

        Set<Long> actualIds = actualBlocks.stream()
                .map(EventBlock::getId)
                .collect(Collectors.toSet());
        Set<Long> requestedIds = items.stream()
                .map(EventBlockReorderRequest.Item::blockId)
                .collect(Collectors.toSet());

        if (actualIds.size() != requestedIds.size() || !actualIds.equals(requestedIds)) {
            throw new BusinessValidationException("request must contain every block of the event exactly once");
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