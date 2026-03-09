package com.gameplatform.mainservice.publication.mapper;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PublicationResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public PublicationResponse toResponse(Publication entity) {
        return new PublicationResponse(
                entity.getId(),
                entity.getType(),
                entity.getTitle(),
                entity.getContent(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey()),
                entity.isPinned(),
                entity.getPublishedAt()
        );
    }

    public List<PublicationResponse> toResponseList(List<Publication> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
