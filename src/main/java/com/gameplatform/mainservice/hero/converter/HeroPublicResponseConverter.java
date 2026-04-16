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
                mediaUrlResolver.resolveUrl(hero.getPreviewBucket(), hero.getPreviewObjectKey()),
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
            Hero currentHero,
            Element element,
            Rarity rarity,
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
                        hero.getElementName(),
                        mediaUrlResolver.resolveUrl(element.getImageBucket(), element.getImageObjectKey())
                ),
                new HeroRarityResponse(
                        hero.getRarityId(),
                        hero.getRarityStars(),
                        mediaUrlResolver.resolveUrl(rarity.getImageBucket(), rarity.getImageObjectKey())
                ),
                new HeroClassDetailsResponse(
                        heroClass.getId(),
                        getLocalized(heroClass.getNameJson(), locale),
                        mediaUrlResolver.resolveUrl(heroClass.getImageBucket(), heroClass.getImageObjectKey()),
                        getLocalized(heroClass.getBaseNameJson(), locale),
                        getLocalized(heroClass.getBaseDescriptionJson(), locale),
                        getLocalized(heroClass.getMasterNameJson(), locale),
                        getLocalized(heroClass.getMasterDescriptionJson(), locale)
                ),
                family != null
                        ? new DescribedReferenceResponse(
                        family.getId(),
                        getLocalized(family.getNameJson(), locale),
                        getLocalized(family.getDescriptionJson(), locale),
                        mediaUrlResolver.resolveUrl(family.getImageBucket(), family.getImageObjectKey())
                )
                        : null,
                new DescribedReferenceResponse(
                        manaSpeed.getId(),
                        getLocalized(manaSpeed.getNameJson(), locale),
                        getLocalized(manaSpeed.getDescriptionJson(), locale),
                        null
                ),
                alphaTalent != null
                        ? new DescribedReferenceResponse(
                        alphaTalent.getId(),
                        getLocalized(alphaTalent.getNameJson(), locale),
                        getLocalized(alphaTalent.getDescriptionJson(), locale),
                        mediaUrlResolver.resolveUrl(alphaTalent.getImageBucket(), alphaTalent.getImageObjectKey())
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
                                getLocalized(ps.getDescriptionJson(), locale),
                                mediaUrlResolver.resolveUrl(ps.getImageBucket(), ps.getImageObjectKey())
                        ))
                        .toList(),

                costumes.stream()
                        .map(costume -> new HeroCostumeResponse(
                                costume.getId(),
                                costume.getSlug(),
                                getLocalized(costume.getNameJson(), locale),
                                costume.getCostumeIndex(),
                                costume.getCostumeBonusJson()
                        ))
                        .toList(),

                hero.getBaseHeroId(),
                currentHero.getBaseAttack(),
                currentHero.getBaseArmor(),
                currentHero.getBaseHp(),
                currentHero.getCostumeBonusJson(),
                mediaUrlResolver.resolveUrl(hero.getImageBucket(), hero.getImageObjectKey()),
                mediaUrlResolver.resolveUrl(hero.getPreviewBucket(), hero.getPreviewObjectKey()),
                hero.getReleaseDate()
        );
    }

    public HeroVariantSummaryResponse toVariantSummary(HeroVariantSummaryProjection hero) {
        return new HeroVariantSummaryResponse(
                hero.getId(),
                hero.getSlug(),
                hero.getName(),
                hero.getCostumeIndex(),
                mediaUrlResolver.resolveUrl(hero.getImageBucket(), hero.getImageObjectKey()),
                mediaUrlResolver.resolveUrl(hero.getPreviewBucket(), hero.getPreviewObjectKey()),
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
