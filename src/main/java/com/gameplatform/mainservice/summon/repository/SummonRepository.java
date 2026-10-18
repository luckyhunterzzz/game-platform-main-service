package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.Summon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SummonRepository extends JpaRepository<Summon, UUID> {
}
