package com.gameplatform.mainservice.hero.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@EqualsAndHashCode
public class HeroPassiveSkillId {
    @Column(name = "hero_id")
    private Long heroId;

    @Column(name = "passive_skill_id")
    private Long passiveSkillId;
}
