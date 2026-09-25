package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.CantidadProducir;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.OrdenProduccionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.RecetaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ProduccionCompletadaEvent(
        UUID eventId,
        Instant occurredOn,
        EmpresaId empresaId,
        OrdenProduccionId ordenProduccionId,
        RecetaId recetaId,
        CantidadProducir cantidadProducida
) {
    public ProduccionCompletadaEvent {
        Objects.requireNonNull(eventId, "El eventId no puede ser nulo");
        Objects.requireNonNull(occurredOn, "El occurredOn no puede ser nulo");
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        Objects.requireNonNull(ordenProduccionId, "El ordenProduccionId no puede ser nulo");
        Objects.requireNonNull(recetaId, "El recetaId no puede ser nulo");
        Objects.requireNonNull(cantidadProducida, "La cantidadProducida no puede ser nula");
    }

    public ProduccionCompletadaEvent(EmpresaId empresaId, OrdenProduccionId ordenProduccionId, RecetaId recetaId, CantidadProducir cantidadProducida) {
        this(UUID.randomUUID(), Instant.now(), empresaId, ordenProduccionId, recetaId, cantidadProducida);
    }
}
