package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.repository.projection.HeroCardProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroCoachHeroProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroDetailsProjection;
import com.gameplatform.mainservice.hero.repository.projection.OutfitterHeroProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroReferenceValidationProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroVariantSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HeroRepository extends JpaRepository<Hero, Long> {

    Optional<Hero> findBySlug(String slug);

    Optional<Hero> findBySlugAndStatus(String slug, HeroStatus status);

    Optional<Hero> findBySlugAndStatusIn(String slug, List<HeroStatus> statuses);

    boolean existsBySlug(String slug);

    Optional<Hero> findByBaseHeroIdAndCostumeIndex(Long baseHeroId, Integer costumeIndex);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query(
            value = """
                    SELECT h.id
                    FROM heroes h
                    WHERE (:search IS NULL
                           OR LOWER(h.slug) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(COALESCE(h.name_json ->> 'ru', '')) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(COALESCE(h.name_json ->> 'en', '')) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:rarityIdsEmpty = true OR h.rarity_id IN (:rarityIds))
                      AND (:statusesEmpty = true OR h.status IN (:statuses))
                    ORDER BY LOWER(COALESCE(h.name_json ->> 'ru', h.name_json ->> 'en', h.slug)) ASC, h.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM heroes h
                    WHERE (:search IS NULL
                           OR LOWER(h.slug) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(COALESCE(h.name_json ->> 'ru', '')) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(COALESCE(h.name_json ->> 'en', '')) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:rarityIdsEmpty = true OR h.rarity_id IN (:rarityIds))
                      AND (:statusesEmpty = true OR h.status IN (:statuses))
                    """,
            nativeQuery = true
    )
    Page<Long> findHeroIdsForAdminCatalog(
            @Param("search") String search,
            @Param("rarityIds") List<Long> rarityIds,
            @Param("rarityIdsEmpty") boolean rarityIdsEmpty,
            @Param("statuses") List<String> statuses,
            @Param("statusesEmpty") boolean statusesEmpty,
            Pageable pageable
    );

    @Query("SELECT COALESCE(MAX(h.costumeIndex), 0) FROM Hero h WHERE h.baseHeroId = :baseHeroId")
    Integer findMaxCostumeIndexByBaseHeroId(@Param("baseHeroId") Long baseHeroId);

    @Query("""
            SELECT h
            FROM Hero h
            JOIN HeroPassiveSkill hps ON hps.id.heroId = h.id
            WHERE hps.id.passiveSkillId = :passiveSkillId
            ORDER BY h.id ASC
            """)
    List<Hero> findAllByPassiveSkillId(@Param("passiveSkillId") Long passiveSkillId);

    @Query(value = """
            SELECT
                EXISTS(SELECT 1 FROM elements e WHERE e.id = :elementId) AS elementExists,
                EXISTS(SELECT 1 FROM rarities r WHERE r.id = :rarityId) AS rarityExists,
                EXISTS(SELECT 1 FROM hero_classes hc WHERE hc.id = :heroClassId) AS heroClassExists,
                (:familyId IS NULL OR EXISTS(SELECT 1 FROM families f WHERE f.id = :familyId)) AS familyExists,
                EXISTS(SELECT 1 FROM mana_speeds ms WHERE ms.id = :manaSpeedId) AS manaSpeedExists,
                (:alphaTalentId IS NULL OR EXISTS(SELECT 1 FROM alpha_talents at WHERE at.id = :alphaTalentId)) AS alphaTalentExists,
                (:baseHeroId IS NULL OR EXISTS(SELECT 1 FROM heroes h WHERE h.id = :baseHeroId)) AS baseHeroExists
            """, nativeQuery = true)
    HeroReferenceValidationProjection validateReferences(
            @Param("elementId") Long elementId,
            @Param("rarityId") Long rarityId,
            @Param("heroClassId") Long heroClassId,
            @Param("familyId") Long familyId,
            @Param("manaSpeedId") Long manaSpeedId,
            @Param("alphaTalentId") Long alphaTalentId,
            @Param("baseHeroId") Long baseHeroId
    );

    @Query(value = """
            SELECT ps.id
            FROM passive_skills ps
            WHERE ps.id IN :ids
            """, nativeQuery = true)
    List<Long> findExistingPassiveSkillIds(@Param("ids") List<Long> ids);

    @Query(
            value = """
                    SELECT
                        h.id AS id,
                        h.slug AS slug,
                        COALESCE(h.name_json ->> :locale, h.slug) AS name,
                        h.base_hero_id AS baseHeroId,
                        h.is_costume AS isCostume,
                        h.costume_index AS costumeIndex,
                        COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                        COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                        h.preview_bucket AS previewBucket,
                        h.preview_object_key AS previewObjectKey,
                        e.name_json ->> :locale AS elementName,
                        r.name_json ->> :locale AS rarityName,
                        r.stars AS rarityStars,
                        hc.name_json ->> :locale AS heroClassName,
                        ms.name_json ->> :locale AS manaSpeedName,
                        f.name_json ->> :locale AS familyName,
                        at.name_json ->> :locale AS alphaTalentName,
                        h.base_attack AS baseAttack,
                        h.base_armor AS baseArmor,
                        h.base_hp AS baseHp,
                        h.release_date AS releaseDate
                    FROM heroes h
                    JOIN elements e ON e.id = h.element_id
                    JOIN rarities r ON r.id = h.rarity_id
                    JOIN hero_classes hc ON hc.id = h.hero_class_id
                    JOIN mana_speeds ms ON ms.id = h.mana_speed_id
                    LEFT JOIN families f ON f.id = h.family_id
                    LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
                    WHERE (h.status = 'READY' OR (:includeDrafts = true AND h.status = 'DRAFT'))
                      AND (:search IS NULL OR LOWER(COALESCE(h.name_json ->> :locale, h.slug)) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:elementIdsEmpty = true OR h.element_id IN (:elementIds))
                      AND (:rarityIdsEmpty = true OR h.rarity_id IN (:rarityIds))
                      AND (:heroClassIdsEmpty = true OR h.hero_class_id IN (:heroClassIds))
                      AND (:familyIdsEmpty = true OR h.family_id IN (:familyIds))
                      AND (:manaSpeedIdsEmpty = true OR h.mana_speed_id IN (:manaSpeedIds))
                      AND (:alphaTalentIdsEmpty = true OR h.alpha_talent_id IN (:alphaTalentIds))
                    ORDER BY h.release_date DESC NULLS LAST,
                             h.is_costume ASC,
                             COALESCE(h.costume_index, 2147483647) ASC,
                             LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC,
                             h.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM heroes h
                    WHERE (h.status = 'READY' OR (:includeDrafts = true AND h.status = 'DRAFT'))
                      AND (:search IS NULL OR LOWER(COALESCE(h.name_json ->> :locale, h.slug)) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:elementIdsEmpty = true OR h.element_id IN (:elementIds))
                      AND (:rarityIdsEmpty = true OR h.rarity_id IN (:rarityIds))
                      AND (:heroClassIdsEmpty = true OR h.hero_class_id IN (:heroClassIds))
                      AND (:familyIdsEmpty = true OR h.family_id IN (:familyIds))
                      AND (:manaSpeedIdsEmpty = true OR h.mana_speed_id IN (:manaSpeedIds))
                      AND (:alphaTalentIdsEmpty = true OR h.alpha_talent_id IN (:alphaTalentIds))
                    """,
            nativeQuery = true
    )
    Page<HeroCardProjection> findReadyHeroCards(
            @Param("locale") String locale,
            @Param("search") String search,
            @Param("elementIds") List<Long> elementIds,
            @Param("elementIdsEmpty") boolean elementIdsEmpty,
            @Param("rarityIds") List<Long> rarityIds,
            @Param("rarityIdsEmpty") boolean rarityIdsEmpty,
            @Param("heroClassIds") List<Long> heroClassIds,
            @Param("heroClassIdsEmpty") boolean heroClassIdsEmpty,
            @Param("familyIds") List<Long> familyIds,
            @Param("familyIdsEmpty") boolean familyIdsEmpty,
            @Param("manaSpeedIds") List<Long> manaSpeedIds,
            @Param("manaSpeedIdsEmpty") boolean manaSpeedIdsEmpty,
            @Param("alphaTalentIds") List<Long> alphaTalentIds,
            @Param("alphaTalentIdsEmpty") boolean alphaTalentIdsEmpty,
            @Param("includeDrafts") boolean includeDrafts,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                h.base_hero_id AS baseHeroId,
                h.is_costume AS isCostume,
                h.costume_index AS costumeIndex,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                e.name_json ->> :locale AS elementName,
                r.name_json ->> :locale AS rarityName,
                r.stars AS rarityStars,
                hc.name_json ->> :locale AS heroClassName,
                ms.name_json ->> :locale AS manaSpeedName,
                f.name_json ->> :locale AS familyName,
                at.name_json ->> :locale AS alphaTalentName,
                h.base_attack AS baseAttack,
                h.base_armor AS baseArmor,
                h.base_hp AS baseHp,
                h.release_date AS releaseDate
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            JOIN hero_classes hc ON hc.id = h.hero_class_id
            JOIN mana_speeds ms ON ms.id = h.mana_speed_id
            LEFT JOIN families f ON f.id = h.family_id
            LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
            WHERE (h.status = 'READY' OR (:includeDrafts = true AND h.status = 'DRAFT'))
              AND h.id IN (:heroIds)
            ORDER BY h.release_date DESC NULLS LAST,
                     h.is_costume ASC,
                     COALESCE(h.costume_index, 2147483647) ASC,
                     LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC,
                     h.id ASC
            """, nativeQuery = true)
    List<HeroCardProjection> findHeroCardsByIds(
            @Param("heroIds") List<Long> heroIds,
            @Param("locale") String locale,
            @Param("includeDrafts") boolean includeDrafts
    );

    @Query(
            value = """
                    SELECT
                        h.id AS id,
                        h.slug AS slug,
                        COALESCE(h.name_json ->> :locale, h.slug) AS name,
                        COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                        COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                        h.preview_bucket AS previewBucket,
                        h.preview_object_key AS previewObjectKey,
                        e.name_json ->> :locale AS elementName,
                        r.name_json ->> :locale AS rarityName,
                        r.stars AS rarityStars,
                        hc.name_json ->> :locale AS heroClassName,
                        ms.name_json ->> :locale AS manaSpeedName,
                        f.name_json ->> :locale AS familyName,
                        at.name_json ->> :locale AS alphaTalentName,
                        h.base_attack AS baseAttack,
                        h.base_armor AS baseArmor,
                        h.base_hp AS baseHp,
                        h.release_date AS releaseDate,
                        (h.release_date + 730) AS heroCoachDate
                    FROM heroes h
                    JOIN elements e ON e.id = h.element_id
                    JOIN rarities r ON r.id = h.rarity_id
                    JOIN hero_classes hc ON hc.id = h.hero_class_id
                    JOIN mana_speeds ms ON ms.id = h.mana_speed_id
                    LEFT JOIN families f ON f.id = h.family_id
                    LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
                    WHERE h.status = 'READY'
                      AND h.is_costume = false
                      AND r.stars = 5
                      AND h.release_date IS NOT NULL
                      AND h.release_date <= :eligibleReleaseDate
                    ORDER BY h.release_date DESC,
                             LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC,
                             h.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM heroes h
                    JOIN rarities r ON r.id = h.rarity_id
                    WHERE h.status = 'READY'
                      AND h.is_costume = false
                      AND r.stars = 5
                      AND h.release_date IS NOT NULL
                      AND h.release_date <= :eligibleReleaseDate
                    """,
            nativeQuery = true
    )
    Page<HeroCoachHeroProjection> findReadyHeroCoachHeroes(
            @Param("locale") String locale,
            @Param("eligibleReleaseDate") java.time.LocalDate eligibleReleaseDate,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                e.name_json ->> :locale AS elementName,
                r.name_json ->> :locale AS rarityName,
                r.stars AS rarityStars,
                hc.name_json ->> :locale AS heroClassName,
                ms.name_json ->> :locale AS manaSpeedName,
                f.name_json ->> :locale AS familyName,
                at.name_json ->> :locale AS alphaTalentName,
                h.base_attack AS baseAttack,
                h.base_armor AS baseArmor,
                h.base_hp AS baseHp,
                h.release_date AS releaseDate,
                (h.release_date + 730) AS heroCoachDate
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            JOIN hero_classes hc ON hc.id = h.hero_class_id
            JOIN mana_speeds ms ON ms.id = h.mana_speed_id
            LEFT JOIN families f ON f.id = h.family_id
            LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
            WHERE h.status = 'READY'
              AND h.is_costume = false
              AND r.stars = 5
              AND h.release_date IS NOT NULL
              AND h.release_date > :previousEligibleReleaseDate
              AND h.release_date <= :targetEligibleReleaseDate
            ORDER BY h.release_date DESC,
                     LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC,
                     h.id ASC
            """, nativeQuery = true)
    List<HeroCoachHeroProjection> findReadyHeroCoachHeroesReleasedBetween(
            @Param("locale") String locale,
            @Param("previousEligibleReleaseDate") java.time.LocalDate previousEligibleReleaseDate,
            @Param("targetEligibleReleaseDate") java.time.LocalDate targetEligibleReleaseDate
    );

    @Query(
            value = """
                    SELECT
                        h.id AS id,
                        h.slug AS slug,
                        COALESCE(h.name_json ->> :locale, h.slug) AS name,
                        COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                        COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                        h.preview_bucket AS previewBucket,
                        h.preview_object_key AS previewObjectKey,
                        e.name_json ->> :locale AS elementName,
                        r.name_json ->> :locale AS rarityName,
                        r.stars AS rarityStars,
                        hc.name_json ->> :locale AS heroClassName,
                        ms.name_json ->> :locale AS manaSpeedName,
                        f.name_json ->> :locale AS familyName,
                        at.name_json ->> :locale AS alphaTalentName,
                        h.base_attack AS baseAttack,
                        h.base_armor AS baseArmor,
                        h.base_hp AS baseHp,
                        h.release_date AS releaseDate,
                        (h.release_date + 549) AS visitingOutfitterDate
                    FROM heroes h
                    JOIN elements e ON e.id = h.element_id
                    JOIN rarities r ON r.id = h.rarity_id
                    JOIN hero_classes hc ON hc.id = h.hero_class_id
                    JOIN mana_speeds ms ON ms.id = h.mana_speed_id
                    LEFT JOIN families f ON f.id = h.family_id
                    LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
                    WHERE h.status = 'READY'
                      AND h.is_costume = true
                      AND h.release_date IS NOT NULL
                      AND h.release_date <= :eligibleReleaseDate
                    ORDER BY h.release_date DESC,
                             LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC,
                             h.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM heroes h
                    WHERE h.status = 'READY'
                      AND h.is_costume = true
                      AND h.release_date IS NOT NULL
                      AND h.release_date <= :eligibleReleaseDate
                    """,
            nativeQuery = true
    )
    Page<OutfitterHeroProjection> findReadyOutfitterHeroes(
            @Param("locale") String locale,
            @Param("eligibleReleaseDate") java.time.LocalDate eligibleReleaseDate,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                e.name_json ->> :locale AS elementName,
                r.name_json ->> :locale AS rarityName,
                r.stars AS rarityStars,
                hc.name_json ->> :locale AS heroClassName,
                ms.name_json ->> :locale AS manaSpeedName,
                f.name_json ->> :locale AS familyName,
                at.name_json ->> :locale AS alphaTalentName,
                h.base_attack AS baseAttack,
                h.base_armor AS baseArmor,
                h.base_hp AS baseHp,
                h.release_date AS releaseDate,
                (h.release_date + 549) AS visitingOutfitterDate
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            JOIN hero_classes hc ON hc.id = h.hero_class_id
            JOIN mana_speeds ms ON ms.id = h.mana_speed_id
            LEFT JOIN families f ON f.id = h.family_id
            LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
            WHERE h.status = 'READY'
              AND h.is_costume = true
              AND h.release_date IS NOT NULL
              AND h.release_date > :previousEligibleReleaseDate
              AND h.release_date <= :targetEligibleReleaseDate
            ORDER BY h.release_date DESC,
                     LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC,
                     h.id ASC
            """, nativeQuery = true)
    List<OutfitterHeroProjection> findReadyOutfitterHeroesReleasedBetween(
            @Param("locale") String locale,
            @Param("previousEligibleReleaseDate") java.time.LocalDate previousEligibleReleaseDate,
            @Param("targetEligibleReleaseDate") java.time.LocalDate targetEligibleReleaseDate
    );

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

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                h.element_id AS elementId,
                e.name_json ->> :locale AS elementName,
                h.rarity_id AS rarityId,
                r.stars AS rarityStars,
                h.hero_class_id AS heroClassId,
                hc.name_json ->> :locale AS heroClassName,
                h.family_id AS familyId,
                f.name_json ->> :locale AS familyName,
                h.mana_speed_id AS manaSpeedId,
                ms.name_json ->> :locale AS manaSpeedName,
                h.alpha_talent_id AS alphaTalentId,
                at.name_json ->> :locale AS alphaTalentName,
                h.special_skill_name_json ->> :locale AS specialSkillName,
                h.special_skill_description_json ->> :locale AS specialSkillDescription,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                h.base_hero_id AS baseHeroId,
                h.is_costume AS isCostume,
                h.release_date AS releaseDate
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            JOIN hero_classes hc ON hc.id = h.hero_class_id
            JOIN mana_speeds ms ON ms.id = h.mana_speed_id
            LEFT JOIN families f ON f.id = h.family_id
            LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
            WHERE h.slug = :slug
              AND h.status = 'READY'
              AND h.is_costume = false
            """, nativeQuery = true)
    Optional<HeroDetailsProjection> findReadyBaseHeroDetailsBySlug(
            @Param("slug") String slug,
            @Param("locale") String locale
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                h.element_id AS elementId,
                e.name_json ->> :locale AS elementName,
                h.rarity_id AS rarityId,
                r.stars AS rarityStars,
                h.hero_class_id AS heroClassId,
                hc.name_json ->> :locale AS heroClassName,
                h.family_id AS familyId,
                f.name_json ->> :locale AS familyName,
                h.mana_speed_id AS manaSpeedId,
                ms.name_json ->> :locale AS manaSpeedName,
                h.alpha_talent_id AS alphaTalentId,
                at.name_json ->> :locale AS alphaTalentName,
                h.special_skill_name_json ->> :locale AS specialSkillName,
                h.special_skill_description_json ->> :locale AS specialSkillDescription,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                h.base_hero_id AS baseHeroId,
                h.is_costume AS isCostume,
                h.release_date AS releaseDate
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            JOIN hero_classes hc ON hc.id = h.hero_class_id
            JOIN mana_speeds ms ON ms.id = h.mana_speed_id
            LEFT JOIN families f ON f.id = h.family_id
            LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
            WHERE h.slug = :slug
              AND (h.status = 'READY' OR (:includeDrafts = true AND h.status = 'DRAFT'))
            """, nativeQuery = true)
    Optional<HeroDetailsProjection> findReadyHeroDetailsBySlug(
            @Param("slug") String slug,
            @Param("locale") String locale,
            @Param("includeDrafts") boolean includeDrafts
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                h.costume_index AS costumeIndex,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                e.name_json ->> :locale AS elementName,
                r.name_json ->> :locale AS rarityName,
                r.stars AS rarityStars
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            WHERE h.id = :id
              AND (h.status = 'READY' OR (:includeDrafts = true AND h.status = 'DRAFT'))
            """, nativeQuery = true)
    Optional<HeroVariantSummaryProjection> findReadyHeroVariantSummaryById(
            @Param("id") Long id,
            @Param("locale") String locale,
            @Param("includeDrafts") boolean includeDrafts
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                h.costume_index AS costumeIndex,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                e.name_json ->> :locale AS elementName,
                r.name_json ->> :locale AS rarityName,
                r.stars AS rarityStars
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            WHERE h.base_hero_id = :baseHeroId
              AND (h.status = 'READY' OR (:includeDrafts = true AND h.status = 'DRAFT'))
            ORDER BY COALESCE(h.costume_index, 2147483647) ASC, h.id ASC
            """, nativeQuery = true)
    List<HeroVariantSummaryProjection> findReadyHeroVariantSummariesByBaseHeroId(
            @Param("baseHeroId") Long baseHeroId,
            @Param("locale") String locale,
            @Param("includeDrafts") boolean includeDrafts
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                h.costume_index AS costumeIndex,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                e.name_json ->> :locale AS elementName,
                r.name_json ->> :locale AS rarityName,
                r.stars AS rarityStars
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            WHERE h.id = :id
            """, nativeQuery = true)
    Optional<HeroVariantSummaryProjection> findHeroVariantSummaryById(
            @Param("id") Long id,
            @Param("locale") String locale
    );

    @Query(value = """
            SELECT
                h.id AS id,
                h.slug AS slug,
                COALESCE(h.name_json ->> :locale, h.slug) AS name,
                h.costume_index AS costumeIndex,
                COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                h.preview_bucket AS previewBucket,
                h.preview_object_key AS previewObjectKey,
                e.name_json ->> :locale AS elementName,
                r.name_json ->> :locale AS rarityName,
                r.stars AS rarityStars
            FROM heroes h
            JOIN elements e ON e.id = h.element_id
            JOIN rarities r ON r.id = h.rarity_id
            WHERE h.base_hero_id = :baseHeroId
            ORDER BY COALESCE(h.costume_index, 2147483647) ASC, h.id ASC
            """, nativeQuery = true)
    List<HeroVariantSummaryProjection> findHeroVariantSummariesByBaseHeroId(
            @Param("baseHeroId") Long baseHeroId,
            @Param("locale") String locale
    );

    List<Hero> findAllByBaseHeroIdAndStatus(
            Long baseHeroId,
            HeroStatus status
    );

    List<Hero> findAllByBaseHeroIdAndStatusIn(
            Long baseHeroId,
            List<HeroStatus> statuses
    );
}
