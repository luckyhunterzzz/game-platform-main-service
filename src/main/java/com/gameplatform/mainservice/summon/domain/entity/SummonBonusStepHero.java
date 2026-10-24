package com.gameplatform.mainservice.summon.domain.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "summon_bonus_step_heroes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummonBonusStepHero {

    @EmbeddedId
    private SummonBonusStepHeroId id;
}
