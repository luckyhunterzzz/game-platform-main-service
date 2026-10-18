package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SummonVariantRepository extends JpaRepository<SummonVariant, UUID> {

    Optional<SummonVariant> findByIdAndSummonId(UUID id, UUID summonId);

    List<SummonVariant> findAllBySummonId(UUID summonId);
}
