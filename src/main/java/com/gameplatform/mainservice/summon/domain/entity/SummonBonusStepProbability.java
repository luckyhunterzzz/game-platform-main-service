package com.gameplatform.mainservice.summon.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "summon_bonus_step_probabilities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummonBonusStepProbability {

    @EmbeddedId
    private SummonBonusStepProbabilityId id;

    @Column(name = "value_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal valuePercent;

    @Column(name = "position", nullable = false)
    private Integer position;
}
