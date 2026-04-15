package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.AlphaTalent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlphaTalentRepository extends JpaRepository<AlphaTalent, Long> {

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM alpha_talents at
            WHERE (:excludedId IS NULL OR at.id <> :excludedId)
              AND (
                    (:ruName IS NOT NULL AND lower(trim(at.name_json ->> 'ru')) = :ruName)
                 OR (:enName IS NOT NULL AND lower(trim(at.name_json ->> 'en')) = :enName)
              )
            """, nativeQuery = true)
    boolean existsDuplicateLocalizedName(
            @Param("ruName") String ruName,
            @Param("enName") String enName,
            @Param("excludedId") Long excludedId
    );
}
