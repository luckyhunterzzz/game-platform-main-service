package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "hero_class_emblem_bonus_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroClassEmblemBonusProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hero_class_id", nullable = false)
    private Long heroClassId;

    @Enumerated(EnumType.STRING)
    @Column(name = "path_type", nullable = false)
    private EmblemPathType pathType;

    @Column(name = "attack_flat_bonus", nullable = false)
    private Integer attackFlatBonus;

    @Column(name = "armor_flat_bonus", nullable = false)
    private Integer armorFlatBonus;

    @Column(name = "hp_flat_bonus", nullable = false)
    private Integer hpFlatBonus;

    @Column(name = "attack_percent_bonus", nullable = false, precision = 6, scale = 4)
    private BigDecimal attackPercentBonus;

    @Column(name = "armor_percent_bonus", nullable = false, precision = 6, scale = 4)
    private BigDecimal armorPercentBonus;

    @Column(name = "hp_percent_bonus", nullable = false, precision = 6, scale = 4)
    private BigDecimal hpPercentBonus;

    @Column(name = "master_attack_bonus", nullable = false)
    private Integer masterAttackBonus;

    @Column(name = "master_armor_bonus", nullable = false)
    private Integer masterArmorBonus;

    @Column(name = "master_hp_bonus", nullable = false)
    private Integer masterHpBonus;
}
