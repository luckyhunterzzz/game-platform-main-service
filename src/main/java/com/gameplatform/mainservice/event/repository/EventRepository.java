package com.gameplatform.mainservice.event.repository;

import com.gameplatform.mainservice.event.domain.entity.Event;
import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findBySlug(String slug);

    Optional<Event> findBySlugAndStatus(String slug, EventStatus status);

    List<Event> findAllByStatusOrderByUpdatedAtDesc(EventStatus status);

    boolean existsBySlug(String slug);
}
