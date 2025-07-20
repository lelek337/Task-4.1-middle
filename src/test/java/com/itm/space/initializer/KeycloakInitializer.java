package com.itm.space.initializer;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class KeycloakInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String KEYCLOAK_IMAGE_NAME = "quay.io/keycloak/keycloak:latest";

    @Container
    public static final KeycloakContainer container = new KeycloakContainer(KEYCLOAK_IMAGE_NAME)
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withRealmImportFile("/keycloak/realm-export.json");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        container.start();

        TestPropertyValues.of(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://" + container.getHost() + ":"
                        + container.getHttpPort() + "/realms/itm-advice/protocol/openid-connect/certs",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://" + container.getHost() + ":"
                        + container.getHttpPort() + "/realms/itm-advice",
                "keycloak.auth-server-url=http://" + container.getHost() + ":" + container.getHttpPort()
        ).applyTo(applicationContext.getEnvironment());
    }
}
