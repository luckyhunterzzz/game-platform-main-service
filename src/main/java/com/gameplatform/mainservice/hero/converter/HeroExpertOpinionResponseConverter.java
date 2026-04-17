package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.HeroExpertOpinion;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionAdminResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionPublicResponse;
import org.springframework.stereotype.Component;

@Component
public class HeroExpertOpinionResponseConverter {

    public HeroExpertOpinionAdminResponse toAdminResponse(HeroExpertOpinion entity) {
        return new HeroExpertOpinionAdminResponse(
                entity.getId(),
                entity.getHeroId(),
                entity.getAuthorName(),
                entity.getSourceUrl(),
                entity.getSourceTitle(),
                entity.getSourceType(),
                entity.getContentJson(),
                entity.isPublished(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public HeroExpertOpinionPublicResponse toPublicResponse(HeroExpertOpinion entity, String locale) {
        return new HeroExpertOpinionPublicResponse(
                entity.getId(),
                entity.getAuthorName(),
                entity.getSourceUrl(),
                entity.getSourceTitle(),
                entity.getSourceType(),
                getLocalized(entity.getContentJson(), locale),
                entity.getPublishedAt()
        );
    }

    private String getLocalized(LocalizedTextJson json, String locale) {
        if (json == null) {
            return null;
        }

        String primaryValue = switch (locale) {
            case "ru" -> json.ru();
            case "en" -> json.en();
            default -> json.ru();
        };

        if (primaryValue != null && !primaryValue.isBlank()) {
            return primaryValue;
        }

        String fallbackValue = switch (locale) {
            case "ru" -> json.en();
            case "en" -> json.ru();
            default -> json.en();
        };

        if (fallbackValue != null && !fallbackValue.isBlank()) {
            return fallbackValue;
        }

        return null;
    }
}
