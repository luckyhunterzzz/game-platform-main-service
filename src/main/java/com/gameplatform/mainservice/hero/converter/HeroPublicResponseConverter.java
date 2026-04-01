package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroPublicResponseConverter {

    private final MediaUrlResolver mediaUrlResolver;

    public List<HeroSimpleNameResponse> toSimpleNameList(List<HeroSearchProjection> projections) {
        return projections.stream()
                .map(p -> new HeroSimpleNameResponse(
                        p.getId(),
                        p.getSlug(),
                        p.getName()
                ))
                .toList();
    }

    public List<HeroSearchResponse> toSearchResponses(List<HeroSearchProjection> projections) {
        return projections.stream()
                .map(p -> new HeroSearchResponse(
                        p.getId(),
                        p.getSlug(),
                        p.getName()
                ))
                .toList();
    }

    public HeroCardResponse toCardResponse(
            Hero hero,
            Element element,
            Rarity rarity,
            HeroClass heroClass,
            Family family,
            ManaSpeed manaSpeed,
            AlphaTalent alphaTalent,
            HeroLanguage language
    ) {
        String locale = language.getJsonKey();

        return new HeroCardResponse(
                hero.getId(),
                hero.getSlug(),
                getLocalized(hero.getNameJson(), locale),
                mediaUrlResolver.resolveUrl(hero.getImageBucket(), hero.getImageObjectKey()),
                getLocalized(element.getNameJson(), locale),
                getLocalized(rarity.getNameJson(), locale),
                rarity.getStars(),
                getLocalized(heroClass.getNameJson(), locale),
                getLocalized(manaSpeed.getNameJson(), locale),
                family != null ? getLocalized(family.getNameJson(), locale) : null,
                alphaTalent != null ? getLocalized(alphaTalent.getNameJson(), locale) : null,
                hero.getBaseAttack(),
                hero.getBaseArmor(),
                hero.getBaseHp()
        );
    }

    public HeroDetailsResponse toDetailsResponse(
            Hero hero,
            Element element,
            Rarity rarity,
            HeroClass heroClass,
            Family family,
            ManaSpeed manaSpeed,
            AlphaTalent alphaTalent,
            List<PassiveSkill> passiveSkills,
            List<Hero> costumes,
            HeroLanguage language
    ) {
        String locale = language.getJsonKey();

        return new HeroDetailsResponse(
                hero.getId(),
                hero.getSlug(),
                getLocalized(hero.getNameJson(), locale),

                new SimpleReferenceResponse(
                        element.getId(),
                        getLocalized(element.getNameJson(), locale)
                ),
                new HeroRarityResponse(
                        rarity.getId(),
                        rarity.getStars()
                ),
                new SimpleReferenceResponse(
                        heroClass.getId(),
                        getLocalized(heroClass.getNameJson(), locale)
                ),
                family != null
                        ? new SimpleReferenceResponse(
                        family.getId(),
                        getLocalized(family.getNameJson(), locale)
                )
                        : null,
                new SimpleReferenceResponse(
                        manaSpeed.getId(),
                        getLocalized(manaSpeed.getNameJson(), locale)
                ),
                alphaTalent != null
                        ? new SimpleReferenceResponse(
                        alphaTalent.getId(),
                        getLocalized(alphaTalent.getNameJson(), locale)
                )
                        : null,

                new SpecialSkillResponse(
                        getLocalized(hero.getSpecialSkillNameJson(), locale),
                        getLocalized(hero.getSpecialSkillDescriptionJson(), locale)
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
