package com.gameplatform.mainservice.outbox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventService outboxEventService;

    @Scheduled(cron = "${app.outbox.dispatch-cron:0 */1 * * * *}")
    public void dispatchPendingEvents() {
        int processedCount = outboxEventService.dispatchPendingEvents();

        if (processedCount > 0) {
            log.info("Dispatched {} outbox events", processedCount);
        }
    }
}
