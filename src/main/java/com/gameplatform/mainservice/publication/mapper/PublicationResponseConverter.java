package com.gameplatform.mainservice.publication.mapper;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationLanguage;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminDetailsResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationAdminSummaryResponse;
import com.gameplatform.mainservice.publication.dto.response.PublicationResponse;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PublicationResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public PublicationResponse toPublicResponse(Publication entity, PublicationLanguage language) {
        return new PublicationResponse(
                entity.getId(),
                entity.getType(),
                getLocalized(entity.getTitleJson(), language),
                getLocalized(entity.getContentJson(), language),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey()),
                entity.isPinned(),
                entity.isShowInNewsFeed(),
                entity.getPublishedAt()
        );
    }

    public PublicationAdminSummaryResponse toAdminSummaryResponse(Publication entity) {
        return new PublicationAdminSummaryResponse(
                entity.getId(),
                entity.getType(),
                entity.getStatus(),
                entity.getTitleJson(),
                entity.getContentJson(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey()),
                entity.isPinned(),
                entity.getPinnedUntil(),
                entity.isShowInNewsFeed(),
                entity.getPublishedAt()
        );
    }

    public PublicationAdminDetailsResponse toAdminDetailsResponse(Publication entity) {
        return new PublicationAdminDetailsResponse(
                entity.getId(),
                entity.getType(),
                entity.getStatus(),
                entity.getTitleJson(),
                entity.getContentJson(),
                entity.getImageBucket(),
                entity.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(entity.getImageBucket(), entity.getImageObjectKey()),
                entity.isPinned(),
                entity.getPinnedUntil(),
                entity.isShowInNewsFeed(),
                entity.getPublishedAt()
        );
    }

    public List<PublicationResponse> toPublicResponseList(
            List<Publication> entities,
            PublicationLanguage language
    ) {
        return entities.stream()
                .map(entity -> toPublicResponse(entity, language))
                .toList();
    }

    public List<PublicationAdminSummaryResponse> toAdminSummaryResponseList(List<Publication> entities) {
        return entities.stream()
                .map(this::toAdminSummaryResponse)
                .toList();
    }

    private String getLocalized(LocalizedTextJson json, PublicationLanguage language) {
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
