package com.gameplatform.mainservice.hero.domain.entity;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "attack_bonus", nullable = false)
    private Integer attackBonus;

    @Column(name = "armor_bonus", nullable = false)
    private Integer armorBonus;

    @Column(name = "hp_bonus", nullable = false)
    private Integer hpBonus;
}
