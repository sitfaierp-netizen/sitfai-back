package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio: Emitido cuando una Recepción es confirmada con éxito.
 */
public record RecepcionConfirmadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        RecepcionId recepcionId
) implements DomainEvent {
    
    public static RecepcionConfirmadaEvent of(EmpresaId empresaId, RecepcionId recepcionId) {
        return new RecepcionConfirmadaEvent(UUID.randomUUID(), Instant.now(), empresaId, recepcionId);
    }
}
