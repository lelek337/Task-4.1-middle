package com.itm.space.util;

import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class AuthUtil {
    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String defaultPassword;
    private static final Logger logger = Logger.getLogger(AuthUtil.class.getName());

    public String getAuthorization(String username, String password) {
        try (Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build()
        ) {
            return "Bearer " + keycloakAdminClient.tokenManager().getAccessToken().getToken();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка получения токена");
            e.getStackTrace();
        }
        return null;
    }

    public String getAuthorization(String username) {
        return this.getAuthorization(username, defaultPassword);
    }

}
