package com.gameplatform.mainservice.summon.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SummonBonusStepProbabilityId implements Serializable {

    @Column(name = "bonus_step_id")
    private UUID bonusStepId;

    @Column(name = "probability_id")
    private UUID probabilityId;
}
