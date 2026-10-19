package com.gameplatform.mainservice.summon.repository;

import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SummonVariantRepository extends JpaRepository<SummonVariant, UUID> {

    Optional<SummonVariant> findByIdAndSummonId(UUID id, UUID summonId);

    List<SummonVariant> findAllBySummonId(UUID summonId);

    Optional<SummonVariant> findFirstBySummonIdAndVariantType(
            UUID summonId,
            SummonVariantType variantType
    );

    List<SummonVariant> findAllBySummonIdAndVariantTypeAndEndsAtAfterOrderByStartsAtAsc(
            UUID summonId,
            SummonVariantType variantType,
            OffsetDateTime now
    );

    Page<SummonVariant> findAllBySummonIdAndVariantTypeAndEndsAtLessThanEqualOrderByEndsAtDesc(
            UUID summonId,
            SummonVariantType variantType,
            OffsetDateTime now,
            Pageable pageable
    );

    List<SummonVariant> findAllByVariantTypeAndStatus(
            SummonVariantType variantType,
            ContentStatus status
    );

    Optional<SummonVariant> findByIdAndVariantTypeAndStatus(
            UUID id,
            SummonVariantType variantType,
            ContentStatus status
    );
}
