package com.gameplatform.mainservice.summon.domain.entity;

import com.gameplatform.mainservice.summon.domain.enums.BonusHeroSelectionMode;
import com.gameplatform.mainservice.summon.domain.enums.BonusRepeatMode;
import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "summon_bonus_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummonBonusRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "probability_id", nullable = false)
    private UUID probabilityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_condition_type", length = 40)
    private BonusResultConditionType triggerConditionType;

    @Column(name = "trigger_rarity_id")
    private Long triggerRarityId;

    @Column(name = "trigger_probability_id")
    private UUID triggerProbabilityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "hero_selection_mode", nullable = false, length = 40)
    private BonusHeroSelectionMode heroSelectionMode;

    @Column(name = "reward_probability_id")
    private UUID rewardProbabilityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_mode", nullable = false, length = 40)
    private BonusRepeatMode repeatMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "continuation_condition_type", length = 40)
    private BonusResultConditionType continuationConditionType;

    @Column(name = "continuation_rarity_id")
    private Long continuationRarityId;

    @Column(name = "continuation_probability_id")
    private UUID continuationProbabilityId;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;
}
