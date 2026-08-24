package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.config;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.application.service.IdempotencyManagerService;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.port.output.IdempotencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotencyConfig {

    @Value("${sitfai.core.idempotency.ttl-millis:86400000}") // Default: 24 hours
    private long ttlMillis;

    @Bean
    public IdempotencyManagerService idempotencyManagerService(IdempotencyRepository repository) {
        return new IdempotencyManagerService(repository, ttlMillis);
    }
}
