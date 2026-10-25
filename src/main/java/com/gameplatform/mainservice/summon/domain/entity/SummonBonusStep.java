package com.gameplatform.mainservice.summon.domain.entity;

import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "summon_bonus_steps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummonBonusStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bonus_rule_id", nullable = false)
    private UUID bonusRuleId;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "activation_chance_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal activationChancePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_mode", nullable = false, length = 40)
    private BonusRewardMode rewardMode;

    @Column(name = "user_hero_selection_enabled", nullable = false)
    private boolean userHeroSelectionEnabled;
}
