package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rarity_evolution_multipliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RarityEvolutionMultiplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rarity_id", nullable = false)
    private Long rarityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_code", nullable = false)
    private EvolutionStageCode stageCode;

    @Column(name = "attack_multiplier", nullable = false, precision = 6, scale = 4)
    private BigDecimal attackMultiplier;

    @Column(name = "armor_multiplier", nullable = false, precision = 6, scale = 4)
    private BigDecimal armorMultiplier;

    @Column(name = "hp_multiplier", nullable = false, precision = 6, scale = 4)
    private BigDecimal hpMultiplier;
}
