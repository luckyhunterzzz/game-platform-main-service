package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonProbability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SummonProbabilityRepository extends JpaRepository<SummonProbability, UUID> {
}
