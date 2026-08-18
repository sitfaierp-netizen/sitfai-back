package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato base inmutable para todos los Eventos de Dominio de IAM.
 */
public interface DomainEvent {
    UUID eventoId();
    Instant ocurridoEn();
    String tipoEvento();
}
