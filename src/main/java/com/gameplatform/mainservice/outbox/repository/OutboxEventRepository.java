package com.gameplatform.mainservice.outbox.repository;

import com.gameplatform.mainservice.outbox.domain.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByProcessedFalseOrderByCreatedAtAsc();
}
