package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RarityRepository extends JpaRepository<Rarity, Long> {

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM rarities r
            WHERE (:excludedId IS NULL OR r.id <> :excludedId)
              AND (
                    (:ruName IS NOT NULL AND lower(trim(r.name_json ->> 'ru')) = :ruName)
                 OR (:enName IS NOT NULL AND lower(trim(r.name_json ->> 'en')) = :enName)
              )
            """, nativeQuery = true)
    boolean existsDuplicateLocalizedName(
            @Param("ruName") String ruName,
            @Param("enName") String enName,
            @Param("excludedId") Long excludedId
    );
}
