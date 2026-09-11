package com.SITFAI_CORE_ERP.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuracion de Seguridad Perimetral Reactiva (Spring Security WebFlux).
 * <p>
 * El Gateway actúa como proxy transparente: NO valida JWT (delegado al backend).
 * La validación de tokens ocurre en sitfai-backend con Spring Security.
 * El CORS global es gestionado por CorsGlobalConfig.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll() // El backend valida JWT; el Gateway solo enruta
            );
        return http.build();
    }
}
