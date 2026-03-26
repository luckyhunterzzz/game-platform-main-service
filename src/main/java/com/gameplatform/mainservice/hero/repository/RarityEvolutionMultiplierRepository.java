package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.RarityEvolutionMultiplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RarityEvolutionMultiplierRepository extends JpaRepository<RarityEvolutionMultiplier, Long> {
}