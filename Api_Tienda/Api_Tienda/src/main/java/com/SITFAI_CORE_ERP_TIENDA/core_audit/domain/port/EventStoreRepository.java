package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port;

/**
 * Driven Port principal para el acceso y persistencia del Event Store (AUD-03, MT-01).
 * <p>
 * Hereda directamente de {@link com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository}
 * asegurando consistencia de convenciones arquitectónicas entre paquetes de dominio.
 */
public interface EventStoreRepository extends com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository {
}
