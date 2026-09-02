package com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.config;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    private final ActorProviderPort actorProviderPort;

    public JpaAuditingConfig(ActorProviderPort actorProviderPort) {
        this.actorProviderPort = actorProviderPort;
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(actorProviderPort.getCurrentActorId());
    }
}
