package com.gameplatform.mainservice.publication.repository;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {

    Page<Publication> findAllByStatus(PublicationStatus status, Pageable pageable);

    Page<Publication> findAllByStatusAndType(PublicationStatus status, PublicationType type, Pageable pageable);

    @Query(value = """
            SELECT p.*
            FROM publications p
            WHERE p.status=:status
            AND (:type IS NULL OR p.type=:type)
            AND (
            :search IS NULL
            OR COALESCE(p.title_json ->> 'ru', '') ILIKE CONCAT('%', :search, '%')
            OR COALESCE(p.title_json ->> 'en', '') ILIKE CONCAT('%', :search, '%')
            )
            ORDER BY
            p.is_pinned DESC,
            p.pinned_at DESC NULLS LAST,
            p.published_at DESC NULLS LAST,
            p.created_at DESC NULLS LAST
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM publications p
                    WHERE p.status=:status
                    AND (:type IS NULL OR p.type=:type)
                    AND (
                    :search IS NULL
                    OR COALESCE(p.title_json ->> 'ru', '') ILIKE CONCAT('%', :search, '%')
                    OR COALESCE(p.title_json ->> 'en', '') ILIKE CONCAT('%', :search, '%')
                    )
                    """,
            nativeQuery = true)
    Page<Publication> searchPublishedByTitle(
            @Param("status") String status,
            @Param("type") String type,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT p.*
                    FROM publications p
                    WHERE p.status = :status
                      AND (:type IS NULL OR p.type = :type)
                      AND (
                            COALESCE(p.title_json ->> 'ru', '') ILIKE CONCAT('%', :search, '%')
                            OR COALESCE(p.title_json ->> 'en', '') ILIKE CONCAT('%', :search, '%')
                      )
                    ORDER BY
                      p.updated_at DESC NULLS LAST,
                      p.created_at DESC NULLS LAST
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM publications p
                    WHERE p.status = :status
                      AND (:type IS NULL OR p.type = :type)
                      AND (
                            COALESCE(p.title_json ->> 'ru', '') ILIKE CONCAT('%', :search, '%')
                            OR COALESCE(p.title_json ->> 'en', '') ILIKE CONCAT('%', :search, '%')
                      )
                    """,
            nativeQuery = true
    )
    Page<Publication> searchDraftsByTitle(
            @Param("status") String status,
            @Param("type") String type,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT p.*
                    FROM publications p
                    WHERE p.status = :status
                      AND (:type IS NULL OR p.type = :type)
                      AND (
                            COALESCE(p.title_json ->> 'ru', '') ILIKE CONCAT('%', :search, '%')
                            OR COALESCE(p.title_json ->> 'en', '') ILIKE CONCAT('%', :search, '%')
                      )
                    ORDER BY
                      p.published_at ASC NULLS LAST,
                      p.created_at DESC NULLS LAST
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM publications p
                    WHERE p.status = :status
                      AND (:type IS NULL OR p.type = :type)
                      AND (
                            COALESCE(p.title_json ->> 'ru', '') ILIKE CONCAT('%', :search, '%')
                            OR COALESCE(p.title_json ->> 'en', '') ILIKE CONCAT('%', :search, '%')
                      )
                    """,
            nativeQuery = true
    )
    Page<Publication> searchScheduledByTitle(
            @Param("status") String status,
            @Param("type") String type,
            @Param("search") String search,
            Pageable pageable
    );

    List<Publication> findAllByStatusAndPublishedAtLessThanEqual(
            PublicationStatus status,
            OffsetDateTime publishedAt
    );

    @Query("""
    SELECT p
    FROM Publication p
    WHERE p.status = :status
    AND p.publishedAt <= :now
    ORDER BY CASE WHEN p.pinned = true AND (p.pinnedUntil IS NULL OR p.pinnedUntil > :now) THEN 1 ELSE 0 END DESC,
             p.pinnedAt DESC, p.publishedAt DESC
    """)
    Page<Publication> findPublishedForPublicFeed(
            @Param("status")PublicationStatus status,
            @Param("now")OffsetDateTime now,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Publication p
    WHERE p.status = :status
    AND p.type = :type
    AND p.publishedAt <= :now
    ORDER BY CASE WHEN p.pinned = true AND (p.pinnedUntil IS NULL OR p.pinnedUntil > :now) THEN 1 ELSE 0 END DESC,
             p.pinnedAt DESC, p.publishedAt DESC
    """)
    Page<Publication> findPublishedForPublicFeedByType(
            @Param("status") PublicationStatus status,
            @Param("type") PublicationType type,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Publication p
    WHERE p.status = :status
    AND p.publishedAt <= :now
    AND (p.type = :newsType OR (p.type = :allianceType AND p.showInNewsFeed = true))
    ORDER BY CASE WHEN p.pinned = true AND (p.pinnedUntil IS NULL OR p.pinnedUntil > :now) THEN 1 ELSE 0 END DESC,
             p.pinnedAt DESC, p.publishedAt DESC
    """)
    Page<Publication> findPublishedForNewsFeed(
            @Param("status") PublicationStatus status,
            @Param("newsType") PublicationType newsType,
            @Param("allianceType") PublicationType allianceType,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );

    List<Publication> findAllByPinnedTrueAndPinnedUntilLessThanEqual(OffsetDateTime pinnedUntil);

    @Query(value = """
            SELECT p.published_at
            FROM publications p
            WHERE p.status = 'PUBLISHED'
              AND p.published_at IS NOT NULL
              AND p.published_at >= :publishedAfter
              AND LOWER(COALESCE(p.title_json ->> 'en', '')) LIKE LOWER(CONCAT('%', :titleFragment, '%'))
            ORDER BY p.published_at DESC
            LIMIT 1
            """, nativeQuery = true)
    java.util.Optional<Instant> findLatestPublishedAtByEnglishTitleContainingSince(
            @Param("titleFragment") String titleFragment,
            @Param("publishedAfter") OffsetDateTime publishedAfter
    );
}
