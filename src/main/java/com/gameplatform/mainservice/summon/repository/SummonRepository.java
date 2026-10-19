package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.Summon;
import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SummonRepository extends JpaRepository<Summon, UUID> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Summon> findAllByOrderByUpdatedAtDesc();

    List<Summon> findAllByStatusOrderByUpdatedAtDesc(ContentStatus status);

    Optional<Summon> findBySlugAndStatus(String slug, ContentStatus status);
}
