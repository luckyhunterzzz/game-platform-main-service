package com.gameplatform.mainservice.kafka.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gameplatform.mainservice.kafka.event.HeroBugReportCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, HeroBugReportCreatedEvent> heroBugReportCreatedKafkaTemplate(
            KafkaProperties kafkaProperties
    ) {
        ObjectMapper kafkaObjectMapper = buildKafkaObjectMapper();
        JsonSerializer<HeroBugReportCreatedEvent> valueSerializer = new JsonSerializer<>(kafkaObjectMapper);

        DefaultKafkaProducerFactory<String, HeroBugReportCreatedEvent> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        buildProducerProperties(kafkaProperties),
                        new StringSerializer(),
                        valueSerializer
                );

        return new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> buildProducerProperties(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    private ObjectMapper buildKafkaObjectMapper() {
        ObjectMapper kafkaObjectMapper = new ObjectMapper();
        kafkaObjectMapper.registerModule(new JavaTimeModule());
        kafkaObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return kafkaObjectMapper;
    }
}
