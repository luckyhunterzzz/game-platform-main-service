package com.gameplatform.mainservice.publication.repository;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {

    Page<Publication> findAllByStatus(PublicationStatus status, Pageable pageable);

    Page<Publication> findAllByStatusAndType(PublicationStatus status, PublicationType type, Pageable pageable);

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
}
