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

    List<Publication> findAllByStatusAndPublishedAtLessThanEqual(
            PublicationStatus status,
            OffsetDateTime publishedAt
    );

    @Query("""
    SELECT p
    FROM Publication p
    WHERE p.status = :status
    AND p.publishedAt <= :now
    ORDER BY p.pinned DESC, p.pinnedAt DESC, p.publishedAt DESC
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
    ORDER BY p.pinned DESC, p.pinnedAt DESC, p.publishedAt DESC
    """)
    Page<Publication> findPublishedForPublicFeedByType(
            @Param("status") PublicationStatus status,
            @Param("type") PublicationType type,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );
}
