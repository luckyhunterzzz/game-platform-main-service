package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeroRepository extends JpaRepository<Hero, Long> {

    Optional<Hero> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Hero> findAllByStatusAndIsCostumeFalse(HeroStatus status, Pageable pageable);

    @Query(value = """
            SELECT 
                h.id AS id,
                h.slug AS slug,
                h.name_json ->> :locale AS name
            FROM heroes h
            WHERE h.status = 'READY'
              AND h.is_costume = false
            ORDER BY LOWER(h.name_json ->> :locale) ASC, h.id ASC
            """, nativeQuery = true)
    List<HeroSearchProjection> findAllReadyBaseHeroNames(@Param("locale") String locale);

    @Query(value = """
        SELECT
            h.id AS id,
            h.slug AS slug,
            COALESCE(h.name_json ->> :locale, h.slug) AS name
        FROM heroes h
        WHERE h.status = 'READY'
          AND h.is_costume = false
          AND LOWER(COALESCE(h.name_json ->> :locale, h.slug)) % LOWER(:query)
        ORDER BY similarity(
                    LOWER(COALESCE(h.name_json ->> :locale, h.slug)),
                    LOWER(:query)
                 ) DESC,
                 LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC,
                 h.id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<HeroSearchProjection> searchReadyBaseHeroesByName(
            @Param("query") String query,
            @Param("locale") String locale,
            @Param("limit") int limit
    );
    // Для детальной карточки
    Optional<Hero> findBySlugAndStatusAndIsCostumeFalse(
            String slug,
            HeroStatus status
    );

    // Для получения костюмов базового героя
    List<Hero> findAllByBaseHeroIdAndStatus(
            Long baseHeroId,
            HeroStatus status
    );

    // Опционально: если хочешь искать без учёта isCostume = false
    Optional<Hero> findBySlugAndStatus(String slug, HeroStatus status);
}