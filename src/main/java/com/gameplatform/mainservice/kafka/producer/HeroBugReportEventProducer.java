package com.gameplatform.mainservice.kafka.producer;

import com.gameplatform.mainservice.kafka.event.HeroBugReportCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HeroBugReportEventProducer {

    private final KafkaTemplate<String, HeroBugReportCreatedEvent> heroBugReportCreatedKafkaTemplate;

    @Value("${app.kafka.topics.hero-bug-report-created}")
    private String heroBugReportCreatedTopic;

    public void sendHeroBugReportCreated(HeroBugReportCreatedEvent event) {
        heroBugReportCreatedKafkaTemplate.send(
                heroBugReportCreatedTopic,
                event.bugReportId().toString(),
                event
        );
    }
}
