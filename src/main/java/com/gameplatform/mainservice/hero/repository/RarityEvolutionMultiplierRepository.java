package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.RarityEvolutionMultiplier;
import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RarityEvolutionMultiplierRepository extends JpaRepository<RarityEvolutionMultiplier, Long> {
    Optional<RarityEvolutionMultiplier> findByRarityIdAndStageCode(Long rarityId, EvolutionStageCode stageCode);
}
