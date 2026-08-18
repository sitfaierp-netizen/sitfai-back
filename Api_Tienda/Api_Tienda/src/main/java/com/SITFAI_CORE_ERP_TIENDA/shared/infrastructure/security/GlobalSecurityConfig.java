package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Objects;

/**
 * Configuración Global de Seguridad para el ecosistema SITFAI ERP (Shared Kernel / Zero Trust).
 * <p>
 * Cumple estrictamente con:
 * <ul>
 *   <li>REGLA 4: Origen criptográfico del tenant vía JWT claims.</li>
 *   <li>REGLA 7: Validación de OAuth2 Resource Server con Keycloak IAM.</li>
 *   <li>ADR-002: Autenticación federada y desacoplamiento de credenciales.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class GlobalSecurityConfig {

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    public GlobalSecurityConfig(KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter) {
        this.keycloakJwtAuthenticationConverter = Objects.requireNonNull(keycloakJwtAuthenticationConverter, "keycloakJwtAuthenticationConverter no puede ser null");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/v1/v3/api-docs/**", "/api/v1/swagger-ui/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter))
            );

        return http.build();
    }
}
