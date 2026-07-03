package com.gameplatform.mainservice.event.repository;

import com.gameplatform.mainservice.event.domain.entity.Event;
import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findBySlug(String slug);

    Optional<Event> findBySlugAndStatus(String slug, EventStatus status);

    List<Event> findAllByStatusOrderByUpdatedAtDesc(EventStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query(
            value = """
                    SELECT e.*
                    FROM events e
                    WHERE (:status IS NULL OR e.status = :status)
                      AND (
                            :search IS NULL
                            OR LOWER(e.slug) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(e.name_json ->> 'ru', '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(e.name_json ->> 'en', '')) LIKE LOWER(CONCAT('%', :search, '%'))
                      )
                    ORDER BY e.updated_at DESC NULLS LAST, e.created_at DESC NULLS LAST
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM events e
                    WHERE (:status IS NULL OR e.status = :status)
                      AND (
                            :search IS NULL
                            OR LOWER(e.slug) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(e.name_json ->> 'ru', '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(e.name_json ->> 'en', '')) LIKE LOWER(CONCAT('%', :search, '%'))
                      )
                    """,
            nativeQuery = true
    )
    Page<Event> findEventsForAdminCatalog(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}
