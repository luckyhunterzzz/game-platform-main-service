package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepProbabilityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SummonBonusStepProbabilityRepository
        extends JpaRepository<SummonBonusStepProbability, SummonBonusStepProbabilityId> {

    List<SummonBonusStepProbability> findAllByIdBonusStepIdIn(Collection<UUID> bonusStepIds);
}
