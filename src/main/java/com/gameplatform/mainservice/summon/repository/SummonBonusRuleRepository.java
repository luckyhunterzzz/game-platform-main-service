package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonBonusRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SummonBonusRuleRepository extends JpaRepository<SummonBonusRule, UUID> {
}
