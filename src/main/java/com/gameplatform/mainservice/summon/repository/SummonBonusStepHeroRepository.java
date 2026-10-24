package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepHeroId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SummonBonusStepHeroRepository
        extends JpaRepository<SummonBonusStepHero, SummonBonusStepHeroId> {

    List<SummonBonusStepHero> findAllByIdBonusStepIdIn(Collection<UUID> bonusStepIds);
}
