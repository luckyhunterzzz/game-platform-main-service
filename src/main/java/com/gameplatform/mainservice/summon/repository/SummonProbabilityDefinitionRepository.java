package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonProbabilityDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SummonProbabilityDefinitionRepository extends JpaRepository<SummonProbabilityDefinition, UUID> {
}
