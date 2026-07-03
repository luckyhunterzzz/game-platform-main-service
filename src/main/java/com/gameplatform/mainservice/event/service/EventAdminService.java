package com.gameplatform.mainservice.event.service;

import com.gameplatform.mainservice.event.domain.entity.Event;
import com.gameplatform.mainservice.event.domain.entity.EventBlock;
import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import com.gameplatform.mainservice.event.dto.request.EventBlockReorderRequest;
import com.gameplatform.mainservice.event.dto.request.EventBlockUpsertRequest;
import com.gameplatform.mainservice.event.dto.request.EventUpsertRequest;
import com.gameplatform.mainservice.event.dto.response.EventAdminDetailsResponse;
import com.gameplatform.mainservice.event.dto.response.EventAdminSummaryResponse;
import com.gameplatform.mainservice.event.mapper.EventResponseConverter;
import com.gameplatform.mainservice.event.repository.EventRepository;
import com.gameplatform.mainservice.event.validation.EventValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventAdminService {

    private static final int TEMP_POSITION_BASE = 10_000;

    private final EventRepository eventRepository;
    private final EventResponseConverter eventResponseConverter;
    private final EventValidator eventValidator;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    @Value("${app.events.page-size-default:10}")
    private int defaultPageSize;

    @Value("${app.events.page-size-max:50}")
    private int maxPageSize;

    public CatalogPageResponse<EventAdminSummaryResponse> getPage(
            EventStatus status,
            String search,
            int page,
            Integer size
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizePageSize(size);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        String normalizedSearch = normalizeSearch(search);
        String statusValue = status == null ? null : status.name();

        Page<EventAdminSummaryResponse> responsePage = eventRepository
                .findEventsForAdminCatalog(statusValue, normalizedSearch, pageable)
                .map(eventResponseConverter::toAdminSummaryResponse);

        return CatalogPageResponse.from(responsePage);
    }

    public EventAdminDetailsResponse getBySlug(String slug) {
        return eventResponseConverter.toAdminDetailsResponse(getEvent(slug));
    }

    @Transactional
    public EventAdminDetailsResponse create(EventUpsertRequest request) {
        eventValidator.validateEventUpsert(request, null);

        OffsetDateTime now = OffsetDateTime.now(clock);
        UUID actorId = currentUserProvider.getUserId();

        Event event = Event.builder()
                .createdBy(actorId)
                .updatedBy(actorId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        applyEventUpsert(event, request, actorId, now);

        Event savedEvent = eventRepository.save(event);
        return eventResponseConverter.toAdminDetailsResponse(savedEvent);
    }

    @Transactional
    public EventAdminDetailsResponse update(String slug, EventUpsertRequest request) {
        Event event = getEvent(slug);
        eventValidator.validateEventUpsert(request, event.getId());

        OffsetDateTime now = OffsetDateTime.now(clock);
        UUID actorId = currentUserProvider.getUserId();

        applyEventUpsert(event, request, actorId, now);

        Event savedEvent = eventRepository.save(event);
        return eventResponseConverter.toAdminDetailsResponse(savedEvent);
    }

    @Transactional
    public void delete(String slug) {
        Event event = getEvent(slug);
        eventRepository.delete(event);
    }

    @Transactional
    public EventAdminDetailsResponse createBlock(String slug, EventBlockUpsertRequest request) {
        eventValidator.validateBlockUpsert(request);

        OffsetDateTime now = OffsetDateTime.now(clock);
        UUID actorId = currentUserProvider.getUserId();

        Event event = getEvent(slug);

        EventBlock block = EventBlock.builder()
                .event(event)
                .position(nextPosition(event))
                .createdBy(actorId)
                .updatedBy(actorId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        applyBlockUpsert(block, request, actorId, now);
        event.getBlocks().add(block);
        touchEvent(event, actorId, now);

        Event savedEvent = eventRepository.save(event);
        return eventResponseConverter.toAdminDetailsResponse(savedEvent);
    }

    @Transactional
    public EventAdminDetailsResponse updateBlock(String slug, Long blockId, EventBlockUpsertRequest request) {
        eventValidator.validateBlockUpsert(request);

        OffsetDateTime now = OffsetDateTime.now(clock);
        UUID actorId = currentUserProvider.getUserId();

        Event event = getEvent(slug);
        EventBlock block = getBlock(event, blockId);

        applyBlockUpsert(block, request, actorId, now);
        touchEvent(event, actorId, now);

        Event savedEvent = eventRepository.save(event);
        return eventResponseConverter.toAdminDetailsResponse(savedEvent);
    }

    @Transactional
    public void deleteBlock(String slug, Long blockId) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        UUID actorId = currentUserProvider.getUserId();

        Event event = getEvent(slug);
        EventBlock block = getBlock(event, blockId);

        event.getBlocks().remove(block);
        touchEvent(event, actorId, now);

        eventRepository.saveAndFlush(event);

        reindexBlocks(event.getBlocks(), actorId, now);
        eventRepository.save(event);
    }

    @Transactional
    public EventAdminDetailsResponse reorderBlocks(String slug, EventBlockReorderRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        UUID actorId = currentUserProvider.getUserId();

        Event event = getEvent(slug);
        eventValidator.validateBlockReorder(event, request);

        assignTemporaryPositions(event.getBlocks());
        touchEvent(event, actorId, now);
        eventRepository.saveAndFlush(event);

        Map<Long, EventBlock> blocksById = event.getBlocks().stream()
                .collect(Collectors.toMap(EventBlock::getId, Function.identity()));

        int position = 1;
        for (Long blockId : request.blockIds()) {
            EventBlock block = blocksById.get(blockId);
            block.setPosition(position++);
            block.setUpdatedBy(actorId);
            block.setUpdatedAt(now);
        }

        Event savedEvent = eventRepository.save(event);
        return eventResponseConverter.toAdminDetailsResponse(savedEvent);
    }

    private Event getEvent(String slug) {
        return eventRepository.findBySlug(normalizeSlug(slug))
                .orElseThrow(() -> new NotFoundException("Event not found: " + slug));
    }

    private EventBlock getBlock(Event event, Long blockId) {
        return event.getBlocks().stream()
                .filter(block -> block.getId().equals(blockId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Event block not found: " + blockId));
    }

    private void applyEventUpsert(Event event, EventUpsertRequest request, UUID actorId, OffsetDateTime now) {
        event.setSlug(normalizeSlug(request.slug()));
        event.setNameJson(request.nameJson());
        event.setDescriptionJson(request.descriptionJson());
        event.setStatus(request.status());
        event.setImageBucket(trimToNull(request.imageBucket()));
        event.setImageObjectKey(trimToNull(request.imageObjectKey()));
        touchEvent(event, actorId, now);
    }

    private void applyBlockUpsert(EventBlock block, EventBlockUpsertRequest request, UUID actorId, OffsetDateTime now) {
        block.setNameJson(request.nameJson());
        block.setDescriptionJson(request.descriptionJson());
        block.setImageBucket(trimToNull(request.imageBucket()));
        block.setImageObjectKey(trimToNull(request.imageObjectKey()));
        block.setVisible(request.visible());
        block.setUpdatedBy(actorId);
        block.setUpdatedAt(now);
    }

    private void touchEvent(Event event, UUID actorId, OffsetDateTime now) {
        event.setUpdatedBy(actorId);
        event.setUpdatedAt(now);
    }

    private int nextPosition(Event event) {
        return event.getBlocks().stream()
                .map(EventBlock::getPosition)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private void reindexBlocks(List<EventBlock> blocks, UUID actorId, OffsetDateTime now) {
        int position = 1;
        for (EventBlock block : blocks.stream().sorted(Comparator.comparing(EventBlock::getPosition)).toList()) {
            block.setPosition(position++);
            block.setUpdatedBy(actorId);
            block.setUpdatedAt(now);
        }
    }

    private void assignTemporaryPositions(List<EventBlock> blocks) {
        int position = TEMP_POSITION_BASE;
        for (EventBlock block : blocks) {
            block.setPosition(position++);
        }
    }

    private int normalizePageSize(Integer size) {
        int resolvedSize = size != null ? size : defaultPageSize;
        return Math.min(Math.max(resolvedSize, 1), maxPageSize);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
