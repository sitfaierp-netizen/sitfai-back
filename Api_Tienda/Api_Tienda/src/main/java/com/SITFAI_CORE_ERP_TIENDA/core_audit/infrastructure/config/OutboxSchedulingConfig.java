package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración de Infraestructura para habilitar el programador de tareas (@Scheduled)
 * necesario para el OutboxEventPoller (AUD-03).
 */
@Configuration
@EnableScheduling
public class OutboxSchedulingConfig {
}
