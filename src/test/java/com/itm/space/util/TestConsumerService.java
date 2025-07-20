package com.itm.space.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@Service
@RequiredArgsConstructor
public class TestConsumerService {

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;


    private final ConsumerFactory<String, byte[]> consumerFactory;
    @SneakyThrows
    public void consumeAndValidate(String topic, byte[] event) {
        try (Consumer<String, byte[]> consumer = consumerFactory.createConsumer(groupId, null)) {
            consumer.subscribe(Collections.singleton(topic));
            ConsumerRecords<String, byte[]> events = consumer.poll(Duration.ofSeconds(10));
            assertThat(events)
                    .hasSizeGreaterThan(0)
                    .extracting(ConsumerRecord::value)
                    .contains(event);
        }
    }
}