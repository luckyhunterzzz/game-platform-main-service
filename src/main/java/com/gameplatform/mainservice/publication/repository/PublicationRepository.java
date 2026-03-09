package com.gameplatform.mainservice.publication.repository;

import com.gameplatform.mainservice.publication.domain.entity.Publication;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {

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
}
