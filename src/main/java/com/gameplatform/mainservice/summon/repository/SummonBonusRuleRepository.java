package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonBonusRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SummonBonusRuleRepository extends JpaRepository<SummonBonusRule, UUID> {

    List<SummonBonusRule> findAllByVariantId(UUID variantId);
}
