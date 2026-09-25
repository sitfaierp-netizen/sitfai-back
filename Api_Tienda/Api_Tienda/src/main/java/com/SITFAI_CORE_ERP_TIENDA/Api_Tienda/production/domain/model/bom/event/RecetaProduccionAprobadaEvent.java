package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.ProductoFinalId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.RecetaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RecetaProduccionAprobadaEvent(
        UUID eventId,
        Instant occurredOn,
        EmpresaId empresaId,
        RecetaId recetaId,
        ProductoFinalId productoFinalId
) {
    public RecetaProduccionAprobadaEvent {
        Objects.requireNonNull(eventId, "El eventId no puede ser nulo");
        Objects.requireNonNull(occurredOn, "El occurredOn no puede ser nulo");
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        Objects.requireNonNull(recetaId, "El recetaId no puede ser nulo");
        Objects.requireNonNull(productoFinalId, "El productoFinalId no puede ser nulo");
    }

    public RecetaProduccionAprobadaEvent(EmpresaId empresaId, RecetaId recetaId, ProductoFinalId productoFinalId) {
        this(UUID.randomUUID(), Instant.now(), empresaId, recetaId, productoFinalId);
    }
}
