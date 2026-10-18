package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SummonBonusStepRepository extends JpaRepository<SummonBonusStep, UUID> {

    List<SummonBonusStep> findAllByBonusRuleIdInOrderByPositionAsc(Collection<UUID> bonusRuleIds);
}
