package org.inventorysystem.inventoryservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static org.inventorysystem.inventoryservice.exception.ErrorCode.KAFKA_PUBLISH_ERROR;

/**
 * Centralized service to publish events to Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes an event to a Kafka topic.
     *
     * @param topic The Kafka topic name.
     * @param key   The message key (optional, can be null).
     * @param event The event payload.
     */
    public void publish(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event);
            log.info("Published event to topic [{}], key: {}, payload: {}", topic, key, event);
        } catch (Exception e) {
            log.error(KAFKA_PUBLISH_ERROR + " - Failed to publish event to topic [{}]: {}", topic, e.getMessage(), e);
        }
    }
}