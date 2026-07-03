package com.gameplatform.mainservice.event.repository;

import com.gameplatform.mainservice.event.domain.entity.EventBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventBlockRepository extends JpaRepository<EventBlock, Long> {

    List<EventBlock> findAllByEventIdOrderByPositionAsc(Long eventId);

    void deleteAllByEventId(Long eventId);
}
