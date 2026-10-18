package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonHeroId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SummonHeroRepository extends JpaRepository<SummonHero, SummonHeroId> {

    List<SummonHero> findAllByIdProbabilityIdIn(Collection<UUID> probabilityIds);
}
