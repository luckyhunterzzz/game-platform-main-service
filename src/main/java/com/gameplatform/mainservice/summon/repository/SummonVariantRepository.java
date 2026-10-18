package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SummonVariantRepository extends JpaRepository<SummonVariant, UUID> {
}
