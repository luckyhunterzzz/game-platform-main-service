package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonHeroId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummonHeroRepository extends JpaRepository<SummonHero, SummonHeroId> {
}
