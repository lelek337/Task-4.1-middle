package com.itm.space;

import org.junit.jupiter.api.Test;

import static com.itm.space.initializer.KafkaInitializer.kafkaContainer;
import static com.itm.space.initializer.PostgresInitializer.postgreSQLContainer;

class RunTest extends BaseIntegrationTest {

    @Test
    void postgreSQLContainerIsRunning() {
        postgreSQLContainer.isRunning();
    }

    @Test
    void kafkaContainerIsRunning() {
        kafkaContainer.isRunning();
    }
}