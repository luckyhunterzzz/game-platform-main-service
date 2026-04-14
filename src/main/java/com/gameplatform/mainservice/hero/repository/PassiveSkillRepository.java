package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.PassiveSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PassiveSkillRepository extends JpaRepository<PassiveSkill, Long> {

    List<PassiveSkill> findAllByIdIn(List<Long> ids);

    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
            FROM passive_skills ps
            WHERE (:excludedId IS NULL OR ps.id <> :excludedId)
              AND (
                    (:ruName IS NOT NULL AND lower(trim(ps.name_json ->> 'ru')) = :ruName)
                 OR (:enName IS NOT NULL AND lower(trim(ps.name_json ->> 'en')) = :enName)
              )
            """, nativeQuery = true)
    boolean existsDuplicateLocalizedName(
            @Param("ruName") String ruName,
            @Param("enName") String enName,
            @Param("excludedId") Long excludedId
    );
}
