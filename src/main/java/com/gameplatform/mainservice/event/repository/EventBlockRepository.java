package com.gameplatform.mainservice.event.repository;

import com.gameplatform.mainservice.event.domain.entity.EventBlock;
import com.gameplatform.mainservice.event.repository.projection.EventBlockCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventBlockRepository extends JpaRepository<EventBlock, Long> {

    List<EventBlock> findAllByEventIdOrderByPositionAsc(Long eventId);

    void deleteAllByEventId(Long eventId);

    @Query("""
            SELECT b.eventId AS eventId,
                   COUNT(b.id) AS blockCount
            FROM EventBlock b
            WHERE b.eventId IN :eventIds
            GROUP BY b.eventId
            """)
    List<EventBlockCountProjection> countByEventIds(@Param("eventIds") List<Long> eventIds);
}