package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.Element;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ElementRepository extends JpaRepository<Element, Long> {

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM elements e
            WHERE (:excludedId IS NULL OR e.id <> :excludedId)
              AND (
                    (:ruName IS NOT NULL AND lower(trim(e.name_json ->> 'ru')) = :ruName)
                 OR (:enName IS NOT NULL AND lower(trim(e.name_json ->> 'en')) = :enName)
              )
            """, nativeQuery = true)
    boolean existsDuplicateLocalizedName(
            @Param("ruName") String ruName,
            @Param("enName") String enName,
            @Param("excludedId") Long excludedId
    );
}
