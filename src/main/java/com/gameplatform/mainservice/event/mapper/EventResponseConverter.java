package com.gameplatform.mainservice.event.mapper;

import com.gameplatform.mainservice.event.domain.entity.Event;
import com.gameplatform.mainservice.event.domain.entity.EventBlock;
import com.gameplatform.mainservice.event.dto.response.EventAdminDetailsResponse;
import com.gameplatform.mainservice.event.dto.response.EventAdminSummaryResponse;
import com.gameplatform.mainservice.event.dto.response.EventBlockAdminResponse;
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
}
