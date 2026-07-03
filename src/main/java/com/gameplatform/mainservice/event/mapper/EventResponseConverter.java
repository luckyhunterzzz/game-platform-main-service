package com.gameplatform.mainservice.event.mapper;

import com.gameplatform.mainservice.event.domain.entity.Event;
import com.gameplatform.mainservice.event.domain.entity.EventBlock;
import com.gameplatform.mainservice.event.domain.enums.EventLanguage;
import com.gameplatform.mainservice.event.dto.response.EventAdminDetailsResponse;
import com.gameplatform.mainservice.event.dto.response.EventAdminSummaryResponse;
import com.gameplatform.mainservice.event.dto.response.EventBlockAdminResponse;
import com.gameplatform.mainservice.event.dto.response.EventBlockResponse;
import com.gameplatform.mainservice.event.dto.response.EventResponse;
import com.gameplatform.mainservice.event.dto.response.EventSummaryResponse;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public EventAdminSummaryResponse toAdminSummaryResponse(Event event) {
        return new EventAdminSummaryResponse(
                event.getId(),
                event.getSlug(),
                event.getStatus(),
                event.getNameJson(),
                event.getDescriptionJson(),
                mediaUrlResolver.resolveUrl(event.getImageBucket(), event.getImageObjectKey()),
                event.getBlocks() == null ? 0 : event.getBlocks().size(),
                event.getUpdatedAt()
        );
    }

    public EventAdminDetailsResponse toAdminDetailsResponse(Event event) {
        return new EventAdminDetailsResponse(
                event.getId(),
                event.getSlug(),
                event.getStatus(),
                event.getNameJson(),
                event.getDescriptionJson(),
                event.getImageBucket(),
                event.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(event.getImageBucket(), event.getImageObjectKey()),
                event.getCreatedBy(),
                event.getUpdatedBy(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                toBlockAdminResponseList(event.getBlocks())
        );
    }

    public EventSummaryResponse toPublicSummaryResponse(Event event, EventLanguage language) {
        return new EventSummaryResponse(
                event.getId(),
                event.getSlug(),
                getLocalized(event.getNameJson(), language),
                getLocalized(event.getDescriptionJson(), language),
                mediaUrlResolver.resolveUrl(event.getImageBucket(), event.getImageObjectKey())
        );
    }

    public EventResponse toPublicResponse(Event event, EventLanguage language) {
        return new EventResponse(
                event.getId(),
                event.getSlug(),
                getLocalized(event.getNameJson(), language),
                getLocalized(event.getDescriptionJson(), language),
                mediaUrlResolver.resolveUrl(event.getImageBucket(), event.getImageObjectKey()),
                toPublicBlockResponseList(event.getBlocks(), language)
        );
    }

    public List<EventBlockAdminResponse> toBlockAdminResponseList(List<EventBlock> blocks) {
        if (blocks == null) {
            return List.of();
        }

        return blocks.stream()
                .sorted(Comparator.comparing(EventBlock::getPosition))
                .map(this::toBlockAdminResponse)
                .toList();
    }

    public EventBlockAdminResponse toBlockAdminResponse(EventBlock block) {
        return new EventBlockAdminResponse(
                block.getId(),
                block.getPosition(),
                block.getNameJson(),
                block.getDescriptionJson(),
                block.getImageBucket(),
                block.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(block.getImageBucket(), block.getImageObjectKey()),
                block.isVisible(),
                block.getCreatedBy(),
                block.getUpdatedBy(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }

    public List<EventBlockResponse> toPublicBlockResponseList(List<EventBlock> blocks, EventLanguage language) {
        if (blocks == null) {
            return List.of();
        }

        return blocks.stream()
                .filter(EventBlock::isVisible)
                .sorted(Comparator.comparing(EventBlock::getPosition))
                .map(block -> toPublicBlockResponse(block, language))
                .toList();
    }

    public EventBlockResponse toPublicBlockResponse(EventBlock block, EventLanguage language) {
        return new EventBlockResponse(
                block.getId(),
                block.getPosition(),
                getLocalized(block.getNameJson(), language),
                getLocalized(block.getDescriptionJson(), language),
                mediaUrlResolver.resolveUrl(block.getImageBucket(), block.getImageObjectKey())
        );
    }

    private String getLocalized(LocalizedTextJson json, EventLanguage language) {
        if (json == null) {
            return null;
        }

        String primaryValue = switch (language) {
            case RU -> json.ru();
            case EN -> json.en();
        };

        if (primaryValue != null && !primaryValue.isBlank()) {
            return primaryValue;
        }

        String fallbackValue = switch (language) {
            case RU -> json.en();
            case EN -> json.ru();
        };

        if (fallbackValue != null && !fallbackValue.isBlank()) {
            return fallbackValue;
        }

        return null;
    }
}
