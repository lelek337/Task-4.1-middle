package com.itm.space;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.spring.api.DBRider;
import com.itm.space.config.UtilConfig;
import com.itm.space.initializer.KafkaInitializer;
import com.itm.space.initializer.KeycloakInitializer;
import com.itm.space.initializer.PostgresInitializer;
import com.itm.space.util.AuthUtil;
import com.itm.space.util.JsonParserUtil;
import com.itm.space.util.TestConsumerService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(initializers = {
        PostgresInitializer.class, KafkaInitializer.class, KeycloakInitializer.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(UtilConfig.class)
@DBRider
@DBUnit(caseSensitiveTableNames = true, schema = "public")
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestConsumerService testConsumerService;

    @Autowired
    protected AuthUtil authUtil;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected JsonParserUtil jsonParserUtil = new JsonParserUtil();
}