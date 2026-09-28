package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.LineaRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record MercanciaRecibidaEvent(
        UUID eventId,
        Instant occurredOn,
        EmpresaId empresaId,
        BodegaId bodegaId,
        RecepcionId recepcionId,
        List<LineaRecepcion> lineasRecibidas
) {
    public MercanciaRecibidaEvent(EmpresaId empresaId, BodegaId bodegaId, RecepcionId recepcionId, List<LineaRecepcion> lineasRecibidas) {
        this(
                UUID.randomUUID(),
                Instant.now(),
                Objects.requireNonNull(empresaId, "EmpresaId es obligatorio en MercanciaRecibidaEvent"),
                Objects.requireNonNull(bodegaId, "BodegaId es obligatorio en MercanciaRecibidaEvent"),
                Objects.requireNonNull(recepcionId, "RecepcionId es obligatorio en MercanciaRecibidaEvent"),
                List.copyOf(Objects.requireNonNull(lineasRecibidas, "LineasRecibidas es obligatorio"))
        );
    }
}
