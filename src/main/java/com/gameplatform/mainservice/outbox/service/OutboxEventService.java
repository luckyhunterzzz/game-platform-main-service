package com.gameplatform.mainservice.outbox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameplatform.mainservice.kafka.event.HeroBugReportCreatedEvent;
import com.gameplatform.mainservice.kafka.producer.HeroBugReportEventProducer;
import com.gameplatform.mainservice.outbox.domain.entity.OutboxEvent;
import com.gameplatform.mainservice.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    public static final String HERO_BUG_REPORT_AGGREGATE_TYPE = "BUG_REPORT";
    public static final String HERO_BUG_REPORT_CREATED_EVENT_TYPE = "HERO_BUG_REPORT_CREATED";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final HeroBugReportEventProducer heroBugReportEventProducer;
    private final Clock clock;

    @Value("${app.outbox.batch-size:100}")
    private int batchSize;

    @Transactional
    public void enqueueHeroBugReportCreated(UUID aggregateId, HeroBugReportCreatedEvent event) {
        JsonNode payload = objectMapper.valueToTree(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType(HERO_BUG_REPORT_AGGREGATE_TYPE)
                .aggregateId(aggregateId)
                .eventType(HERO_BUG_REPORT_CREATED_EVENT_TYPE)
                .payload(payload)
                .createdAt(OffsetDateTime.now(clock))
                .processed(false)
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public int dispatchPendingEvents() {
        List<OutboxEvent> candidates = outboxEventRepository.findTop100ByProcessedFalseOrderByCreatedAtAsc();
        if (candidates.isEmpty()) {
            return 0;
        }

        int limit = Math.min(batchSize, candidates.size());
        int processedCount = 0;

        for (int index = 0; index < limit; index++) {
            OutboxEvent event = candidates.get(index);

            if (!HERO_BUG_REPORT_CREATED_EVENT_TYPE.equals(event.getEventType())) {
                log.warn("Skipping unsupported outbox event type: {}", event.getEventType());
                continue;
            }

            HeroBugReportCreatedEvent payload = objectMapper.convertValue(
                    event.getPayload(),
                    HeroBugReportCreatedEvent.class
            );

            heroBugReportEventProducer.sendHeroBugReportCreated(payload);
            event.setProcessed(true);
            event.setProcessedAt(OffsetDateTime.now(clock));
            processedCount++;
        }

        return processedCount;
    }
}
