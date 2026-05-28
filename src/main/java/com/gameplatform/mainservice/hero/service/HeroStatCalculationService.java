package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.HeroClassEmblemBonusProfile;
import com.gameplatform.mainservice.hero.domain.entity.RarityEvolutionMultiplier;
import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroStatBlockResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroStatCalculationResponse;
import com.gameplatform.mainservice.hero.repository.HeroClassEmblemBonusProfileRepository;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.RarityEvolutionMultiplierRepository;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class HeroStatCalculationService {

    private final HeroRepository heroRepository;
    private final RarityEvolutionMultiplierRepository rarityEvolutionMultiplierRepository;
    private final HeroClassEmblemBonusProfileRepository heroClassEmblemBonusProfileRepository;

    public HeroStatCalculationResponse calculate(Long heroId, HeroStatCalculationRequest request) {
        Hero hero = heroRepository.findById(heroId)
                .orElseThrow(() -> new NotFoundException("Hero not found: " + heroId));

        requireBaseStats(hero);

        boolean includeMasterEmblems = Boolean.TRUE.equals(request.includeMasterEmblems());
        if (includeMasterEmblems && request.emblemPathType() == null) {
            throw new BusinessValidationException("Master emblems require emblemPathType");
        }

        RarityEvolutionMultiplier ascension480 = getMultiplier(hero.getRarityId(), EvolutionStageCode.ASCENSION_4_80);
        RarityEvolutionMultiplier selectedStageMultiplier = getMultiplier(hero.getRarityId(), request.stageCode());

        Hero costumeHero = resolveTargetCostumeHero(hero, request.costumeHeroId());
        HeroClassEmblemBonusProfile emblemProfile = resolveEmblemProfile(hero.getHeroClassId(), request.emblemPathType());

        HeroStatBlockResponse baseStats = new HeroStatBlockResponse(
                hero.getBaseAttack(),
                hero.getBaseArmor(),
                hero.getBaseHp()
        );

        HeroStatBlockResponse effectiveBaseStats;
        HeroStatBlockResponse costumeBonus;

        if (hero.isCostume()) {
            CostumeBonusJson currentCostumeBonusJson = hero.getCostumeBonusJson();
            if (currentCostumeBonusJson == null) {
                throw new BusinessValidationException("Current costume hero does not have costumeBonusJson");
            }

            HeroStatBlockResponse restoredBaseStats = removeCostumeBonus(baseStats, currentCostumeBonusJson);

            if (costumeHero == null) {
                effectiveBaseStats = baseStats;
                costumeBonus = zeroStats();
            } else {
                costumeBonus = calculateCostumeBonus(restoredBaseStats, costumeHero.getCostumeBonusJson());
                effectiveBaseStats = new HeroStatBlockResponse(
                        restoredBaseStats.attack() + costumeBonus.attack(),
                        restoredBaseStats.armor() + costumeBonus.armor(),
                        restoredBaseStats.hp() + costumeBonus.hp()
                );
            }
        } else {
            costumeBonus = costumeHero == null
                    ? zeroStats()
                    : calculateCostumeBonus(baseStats, costumeHero.getCostumeBonusJson());

            effectiveBaseStats = new HeroStatBlockResponse(
                    baseStats.attack() + costumeBonus.attack(),
                    baseStats.armor() + costumeBonus.armor(),
                    baseStats.hp() + costumeBonus.hp()
            );
        }

        HeroStatBlockResponse minStats = new HeroStatBlockResponse(
                ceilDivide(effectiveBaseStats.attack(), ascension480.getAttackMultiplier()),
                ceilDivide(effectiveBaseStats.armor(), ascension480.getArmorMultiplier()),
                ceilDivide(effectiveBaseStats.hp(), ascension480.getHpMultiplier())
        );

        HeroStatBlockResponse stageStats = switch (request.stageCode()) {
            case ASCENSION_4_80 -> effectiveBaseStats;
            case ASCENSION_4_85, ASCENSION_4_90 -> new HeroStatBlockResponse(
                    multiplyFloor(minStats.attack(), selectedStageMultiplier.getAttackMultiplier()),
                    multiplyFloor(minStats.armor(), selectedStageMultiplier.getArmorMultiplier()),
                    multiplyFloor(minStats.hp(), selectedStageMultiplier.getHpMultiplier())
            );
        };

        HeroStatBlockResponse emblemBonus = emblemProfile == null
                ? zeroStats()
                : calculateEmblemBonus(effectiveBaseStats, emblemProfile);

        HeroStatBlockResponse masterEmblemBonus = includeMasterEmblems && emblemProfile != null
                ? new HeroStatBlockResponse(
                        emblemProfile.getMasterAttackBonus(),
                        emblemProfile.getMasterArmorBonus(),
                        emblemProfile.getMasterHpBonus()
                )
                : zeroStats();

        HeroStatBlockResponse finalStats = new HeroStatBlockResponse(
                stageStats.attack() + emblemBonus.attack() + masterEmblemBonus.attack(),
                stageStats.armor() + emblemBonus.armor() + masterEmblemBonus.armor(),
                stageStats.hp() + emblemBonus.hp() + masterEmblemBonus.hp()
        );

        return new HeroStatCalculationResponse(
                request.stageCode(),
                costumeHero != null ? costumeHero.getId() : null,
                costumeHero != null ? costumeHero.getCostumeIndex() : null,
                request.emblemPathType(),
                includeMasterEmblems,
                minStats,
                baseStats,
                stageStats,
                costumeBonus,
                emblemBonus,
                masterEmblemBonus,
                finalStats
        );
    }

    private void requireBaseStats(Hero hero) {
        if (hero.getBaseAttack() == null || hero.getBaseArmor() == null || hero.getBaseHp() == null) {
            throw new BusinessValidationException("Hero must have base stats for calculation");
        }
    }

    private RarityEvolutionMultiplier getMultiplier(Long rarityId, EvolutionStageCode stageCode) {
        return rarityEvolutionMultiplierRepository.findByRarityIdAndStageCode(rarityId, stageCode)
                .orElseThrow(() -> new NotFoundException(
                        "RarityEvolutionMultiplier not found for rarityId=" + rarityId + ", stageCode=" + stageCode
                ));
    }

    private Hero resolveTargetCostumeHero(Hero hero, Long costumeHeroId) {
        if (costumeHeroId == null) {
            return null;
        }

        Hero costumeHero = heroRepository.findById(costumeHeroId)
                .orElseThrow(() -> new NotFoundException("Costume hero not found: " + costumeHeroId));

        if (!costumeHero.isCostume()) {
            throw new BusinessValidationException("Selected hero is not a costume");
        }

        if (costumeHero.getCostumeBonusJson() == null) {
            throw new BusinessValidationException("Selected costume does not have costumeBonusJson");
        }

        if (hero.isCostume()) {
            if (!hero.getBaseHeroId().equals(costumeHero.getBaseHeroId())) {
                throw new BusinessValidationException("Selected costume does not belong to this hero costume line");
            }

            if (hero.getCostumeIndex() == null || costumeHero.getCostumeIndex() == null) {
                throw new BusinessValidationException("Costume index is required for costume recalculation");
            }

            if (costumeHero.getId().equals(hero.getId())) {
                throw new BusinessValidationException("Current costume is already selected");
            }

            if (costumeHero.getCostumeIndex() <= hero.getCostumeIndex()) {
                throw new BusinessValidationException("Only higher costume index can be selected for costume recalculation");
            }
        } else if (!hero.getId().equals(costumeHero.getBaseHeroId())) {
            throw new BusinessValidationException("Selected costume does not belong to this base hero");
        }

        return costumeHero;
    }

    private HeroClassEmblemBonusProfile resolveEmblemProfile(Long heroClassId, EmblemPathType emblemPathType) {
        if (emblemPathType == null) {
            return null;
        }

        return heroClassEmblemBonusProfileRepository.findByHeroClassIdAndPathType(heroClassId, emblemPathType)
                .orElseThrow(() -> new NotFoundException(
                        "HeroClassEmblemBonusProfile not found for heroClassId=" + heroClassId + ", pathType=" + emblemPathType
                ));
    }

    private HeroStatBlockResponse calculateCostumeBonus(HeroStatBlockResponse baseStats, CostumeBonusJson costumeBonusJson) {
        return new HeroStatBlockResponse(
                calculatePercentBonus(baseStats.attack(), costumeBonusJson.attack()),
                calculatePercentBonus(baseStats.armor(), costumeBonusJson.armor()),
                calculatePercentBonus(baseStats.hp(), costumeBonusJson.hp())
        );
    }

    private HeroStatBlockResponse calculateEmblemBonus(
            HeroStatBlockResponse baseStats,
            HeroClassEmblemBonusProfile emblemProfile
    ) {
        return new HeroStatBlockResponse(
                calculatePercentAndFlatBonus(baseStats.attack(), emblemProfile.getAttackPercentBonus(), emblemProfile.getAttackFlatBonus()),
                calculatePercentAndFlatBonus(baseStats.armor(), emblemProfile.getArmorPercentBonus(), emblemProfile.getArmorFlatBonus()),
                calculatePercentAndFlatBonus(baseStats.hp(), emblemProfile.getHpPercentBonus(), emblemProfile.getHpFlatBonus())
        );
    }

    private int ceilDivide(Integer value, BigDecimal multiplier) {
        return BigDecimal.valueOf(value)
                .divide(multiplier, 0, RoundingMode.CEILING)
                .intValueExact();
    }

    private int multiplyFloor(Integer value, BigDecimal multiplier) {
        return BigDecimal.valueOf(value)
                .multiply(multiplier)
                .setScale(0, RoundingMode.FLOOR)
                .intValueExact();
    }

    private int calculatePercentBonus(Integer baseValue, Integer percentValue) {
        if (percentValue == null || percentValue == 0) {
            return 0;
        }

        return BigDecimal.valueOf(baseValue)
                .multiply(BigDecimal.valueOf(percentValue).movePointLeft(2))
                .setScale(0, RoundingMode.FLOOR)
                .intValueExact();
    }

    private HeroStatBlockResponse removeCostumeBonus(HeroStatBlockResponse costumeStats, CostumeBonusJson costumeBonusJson) {
        return new HeroStatBlockResponse(
                reversePercentBonus(costumeStats.attack(), costumeBonusJson.attack()),
                reversePercentBonus(costumeStats.armor(), costumeBonusJson.armor()),
                reversePercentBonus(costumeStats.hp(), costumeBonusJson.hp())
        );
    }

    private int reversePercentBonus(Integer valueWithCostume, Integer percentValue) {
        if (percentValue == null || percentValue == 0) {
            return valueWithCostume;
        }

        BigDecimal multiplier = BigDecimal.ONE.add(BigDecimal.valueOf(percentValue).movePointLeft(2));
        return BigDecimal.valueOf(valueWithCostume)
                .divide(multiplier, 0, RoundingMode.CEILING)
                .intValueExact();
    }

    private int calculatePercentAndFlatBonus(Integer baseValue, BigDecimal percentBonus, Integer flatBonus) {
        BigDecimal percentPart = BigDecimal.valueOf(baseValue)
                .multiply(percentBonus)
                .setScale(0, RoundingMode.FLOOR);

        return percentPart.intValueExact() + flatBonus;
    }

    private HeroStatBlockResponse zeroStats() {
        return new HeroStatBlockResponse(0, 0, 0);
    }
}

