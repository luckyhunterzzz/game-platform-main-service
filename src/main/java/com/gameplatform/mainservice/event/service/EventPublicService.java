package com.gameplatform.mainservice.event.service;

import com.gameplatform.mainservice.event.domain.entity.Event;
import com.gameplatform.mainservice.event.domain.entity.EventBlock;
import com.gameplatform.mainservice.event.domain.enums.EventLanguage;
import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import com.gameplatform.mainservice.event.dto.response.EventFeedResponse;
import com.gameplatform.mainservice.event.dto.response.EventResponse;
import com.gameplatform.mainservice.event.dto.response.EventSummaryResponse;
import com.gameplatform.mainservice.event.mapper.EventResponseConverter;
import com.gameplatform.mainservice.event.repository.EventBlockRepository;
import com.gameplatform.mainservice.event.repository.EventRepository;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EventPublicService {

    private final EventRepository eventRepository;
    private final EventBlockRepository eventBlockRepository;
    private final EventResponseConverter eventResponseConverter;

    @Value("${app.events.page-size-default:10}")
    private int defaultPageSize;

    @Value("${app.events.page-size-max:50}")
    private int maxPageSize;

    public EventFeedResponse getEvents(int page, Integer size, EventLanguage language) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizePageSize(size);

        Page<Event> eventPage = eventRepository.findAllByStatusOrderByUpdatedAtDesc(
                EventStatus.READY,
                PageRequest.of(normalizedPage, normalizedSize)
        );

        List<EventSummaryResponse> items = eventPage.getContent().stream()
                .map(event -> eventResponseConverter.toPublicSummaryResponse(event, language))
                .toList();

        return new EventFeedResponse(
                items,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages(),
                eventPage.hasNext()
        );
    }

    public EventResponse getBySlug(String slug, EventLanguage language) {
        Event event = eventRepository.findBySlugAndStatus(normalizeSlug(slug), EventStatus.READY)
                .orElseThrow(() -> new NotFoundException("Event not found: " + slug));
        List<EventBlock> blocks = eventBlockRepository.findAllByEventIdOrderByPositionAsc(event.getId());
        return eventResponseConverter.toPublicResponse(event, language, blocks);
    }

    private int normalizePageSize(Integer size) {
        int requestedSize = size != null ? size : defaultPageSize;
        return Math.min(Math.max(requestedSize, 1), maxPageSize);
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}