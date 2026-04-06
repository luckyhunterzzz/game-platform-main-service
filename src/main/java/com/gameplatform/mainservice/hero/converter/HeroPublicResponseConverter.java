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
            HeroClass heroClass,
            Family family,
            ManaSpeed manaSpeed,
            AlphaTalent alphaTalent,
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
                new HeroClassDetailsResponse(
                        heroClass.getId(),
                        getLocalized(heroClass.getNameJson(), locale),
                        getLocalized(heroClass.getBaseNameJson(), locale),
                        getLocalized(heroClass.getBaseDescriptionJson(), locale),
                        getLocalized(heroClass.getMasterNameJson(), locale),
                        getLocalized(heroClass.getMasterDescriptionJson(), locale)
                ),
                family != null
                        ? new DescribedReferenceResponse(
                        family.getId(),
                        getLocalized(family.getNameJson(), locale),
                        getLocalized(family.getDescriptionJson(), locale)
                )
                        : null,
                new DescribedReferenceResponse(
                        manaSpeed.getId(),
                        getLocalized(manaSpeed.getNameJson(), locale),
                        getLocalized(manaSpeed.getDescriptionJson(), locale)
                ),
                alphaTalent != null
                        ? new DescribedReferenceResponse(
                        alphaTalent.getId(),
                        getLocalized(alphaTalent.getNameJson(), locale),
                        getLocalized(alphaTalent.getDescriptionJson(), locale)
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
