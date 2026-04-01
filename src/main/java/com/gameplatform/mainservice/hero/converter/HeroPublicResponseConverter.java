package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.repository.projection.HeroCardProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroDetailsProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroVariantSummaryProjection;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroPublicResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public List<HeroLookupResponse> toLookupResponses(List<HeroSearchProjection> projections) {
        return projections.stream()
                .map(p -> new HeroLookupResponse(
                        p.getId(),
                        p.getSlug(),
                        p.getName()
                ))
                .toList();
    }

    public HeroCardResponse toCardResponse(HeroCardProjection hero) {
        return new HeroCardResponse(
                hero.getId(),
                hero.getSlug(),
                hero.getName(),
                mediaUrlResolver.resolveUrl(hero.getImageBucket(), hero.getImageObjectKey()),
                hero.getElementName(),
                hero.getRarityName(),
                hero.getRarityStars(),
                hero.getHeroClassName(),
                hero.getManaSpeedName(),
                hero.getFamilyName(),
                hero.getAlphaTalentName(),
                hero.getBaseAttack(),
                hero.getBaseArmor(),
                hero.getBaseHp()
        );
    }

    public HeroDetailsResponse toDetailsResponse(
            HeroDetailsProjection hero,
            List<PassiveSkill> passiveSkills,
            List<Hero> costumes,
            String locale
    ) {
        return new HeroDetailsResponse(
                hero.getId(),
                hero.getSlug(),
                hero.getName(),

                new SimpleReferenceResponse(
                        hero.getElementId(),
                        hero.getElementName()
                ),
                new HeroRarityResponse(
                        hero.getRarityId(),
                        hero.getRarityStars()
                ),
                new SimpleReferenceResponse(
                        hero.getHeroClassId(),
                        hero.getHeroClassName()
                ),
                hero.getFamilyId() != null
                        ? new SimpleReferenceResponse(
                        hero.getFamilyId(),
                        hero.getFamilyName()
                )
                        : null,
                new SimpleReferenceResponse(
                        hero.getManaSpeedId(),
                        hero.getManaSpeedName()
                ),
                hero.getAlphaTalentId() != null
                        ? new SimpleReferenceResponse(
                        hero.getAlphaTalentId(),
                        hero.getAlphaTalentName()
                )
                        : null,

                new SpecialSkillResponse(
                        hero.getSpecialSkillName(),
                        hero.getSpecialSkillDescription()
                ),

                passiveSkills.stream()
                        .map(ps -> new HeroPassiveSkillResponse(
                                ps.getId(),
                                getLocalized(ps.getNameJson(), locale),
                                getLocalized(ps.getDescriptionJson(), locale)
                        ))
                        .toList(),

                costumes.stream()
                        .map(costume -> new HeroCostumeResponse(
                                costume.getId(),
                                costume.getSlug(),
                                getLocalized(costume.getNameJson(), locale),
                                costume.getCostumeBonusJson()
                        ))
                        .toList(),

                hero.getBaseHeroId(),
                mediaUrlResolver.resolveUrl(hero.getImageBucket(), hero.getImageObjectKey()),
                hero.getReleaseDate()
        );
    }

    public HeroVariantSummaryResponse toVariantSummary(HeroVariantSummaryProjection hero) {
        return new HeroVariantSummaryResponse(
                hero.getId(),
                hero.getSlug(),
                hero.getName(),
                mediaUrlResolver.resolveUrl(hero.getImageBucket(), hero.getImageObjectKey()),
                hero.getElementName(),
                hero.getRarityName(),
                hero.getRarityStars()
        );
    }

    private String getLocalized(LocalizedTextJson json, String locale) {
        if (json == null) {
            return null;
        }

        return switch (locale) {
            case "ru" -> json.ru();
            case "en" -> json.en();
            default -> json.ru();
        };
    }
}
