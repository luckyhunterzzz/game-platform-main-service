package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.HeroClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HeroClassRepository extends JpaRepository<HeroClass, Long> {

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM hero_classes hc
            WHERE (:excludedId IS NULL OR hc.id <> :excludedId)
              AND (
                    (:ruName IS NOT NULL AND lower(trim(hc.name_json ->> 'ru')) = :ruName)
                 OR (:enName IS NOT NULL AND lower(trim(hc.name_json ->> 'en')) = :enName)
              )
            """, nativeQuery = true)
    boolean existsDuplicateLocalizedName(
            @Param("ruName") String ruName,
            @Param("enName") String enName,
            @Param("excludedId") Long excludedId
    );
}
