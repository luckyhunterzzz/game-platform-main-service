package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.Summon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SummonRepository extends JpaRepository<Summon, UUID> {

    boolean existsBySlug(String slug);

    List<Summon> findAllByOrderByUpdatedAtDesc();
}
