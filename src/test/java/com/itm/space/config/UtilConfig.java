package com.itm.space.config;


import com.itm.space.initializer.KeycloakInitializer;
import com.itm.space.util.AuthUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UtilConfig {
    private static final String REALM = "itm-advice";
    private static final String CLIENT_ID = "user-service";
    private static final String CLIENT_SECRET = "**********";
    private static final String DEFAULT_PASSWORD = "1234";

    @Bean
    public AuthUtil authUtil() {
        String keycloakUrl = KeycloakInitializer.container.getAuthServerUrl();
        return new AuthUtil(keycloakUrl, REALM, CLIENT_ID, CLIENT_SECRET, DEFAULT_PASSWORD);
    }
}