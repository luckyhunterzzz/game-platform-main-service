package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HeroTagRepository extends JpaRepository<HeroTag, Long> {

    List<HeroTag> findAllByIdIn(List<Long> ids);

    List<HeroTag> findAllByGroupId(Long groupId);

    boolean existsByGroupId(Long groupId);

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM hero_tags t
            WHERE (:excludedId IS NULL OR t.id <> :excludedId)
              AND (
                    (:ruName IS NOT NULL AND lower(trim(t.name_json ->> 'ru')) = :ruName)
                 OR (:enName IS NOT NULL AND lower(trim(t.name_json ->> 'en')) = :enName)
              )
            """, nativeQuery = true)
    boolean existsDuplicateLocalizedName(
            @Param("ruName") String ruName,
            @Param("enName") String enName,
            @Param("excludedId") Long excludedId
    );
}
