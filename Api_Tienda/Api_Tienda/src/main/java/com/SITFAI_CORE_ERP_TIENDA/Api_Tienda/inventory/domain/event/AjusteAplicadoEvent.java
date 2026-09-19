package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.AjusteInventarioId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio: Emitido cuando un Ajuste de Inventario es confirmado y aplicado a la Bodega.
 */
public record AjusteAplicadoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        BodegaId bodegaId,
        AjusteInventarioId ajusteId
) implements DomainEvent {

    public static AjusteAplicadoEvent of(EmpresaId empresaId, BodegaId bodegaId, AjusteInventarioId ajusteId) {
        return new AjusteAplicadoEvent(UUID.randomUUID(), Instant.now(), empresaId, bodegaId, ajusteId);
    }
}
