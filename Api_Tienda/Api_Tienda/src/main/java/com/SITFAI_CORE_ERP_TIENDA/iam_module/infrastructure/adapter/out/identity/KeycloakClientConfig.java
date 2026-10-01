package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.identity;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.JacksonProvider;
import org.keycloak.OAuth2Constants;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuración del cliente administrador de Keycloak (ADR-002, ADR-007).
 */
@Configuration
public class KeycloakClientConfig {

    @Value("${keycloak.admin.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.admin.auth-realm:sitfai-erp}")
    private String authRealm;

    @Value("${keycloak.admin.client-id:sitfai-backend-admin}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin.connect-timeout:3s}")
    private Duration connectTimeout;

    @Value("${keycloak.admin.read-timeout:5s}")
    private Duration readTimeout;

    @Bean(destroyMethod = "close")
    public Keycloak keycloakAdminClient() {
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("KEYCLOAK_ADMIN_CLIENT_SECRET es obligatorio.");
        }

        var restClient = new ResteasyClientBuilderImpl()
                .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .connectionPoolSize(10)
                .build()
                .register(JacksonProvider.class, 100);

        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(authRealm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .resteasyClient(restClient)
                .build();
    }
}
