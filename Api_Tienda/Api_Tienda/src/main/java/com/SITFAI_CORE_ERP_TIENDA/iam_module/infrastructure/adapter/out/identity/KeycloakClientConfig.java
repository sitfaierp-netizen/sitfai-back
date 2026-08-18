package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.identity;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del cliente administrador de Keycloak (ADR-002, ADR-007).
 */
@Configuration
public class KeycloakClientConfig {

    @Value("${keycloak.admin.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.admin.auth-realm:master}")
    private String authRealm;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String clientId;

    @Value("${keycloak.admin.username:admin}")
    private String username;

    @Value("${keycloak.admin.password:admin}")
    private String password;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(authRealm)
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();
    }
}
