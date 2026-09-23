package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando se recepciona mercancía física en la bodega
 * referenciando un documento fuente upstream (BOD-04, MT-01).
 */
public record IngresoStockRegistradoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        BodegaId bodegaId,
        DocumentoFuenteId documentoFuente,
        int totalLotes
) implements DomainEvent {

    public IngresoStockRegistradoEvent {
        Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
        Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01)");
        Objects.requireNonNull(bodegaId, "bodegaId es obligatorio");
        Objects.requireNonNull(documentoFuente, "documentoFuente es obligatorio (BOD-04)");
    }

    public static IngresoStockRegistradoEvent of(
            EmpresaId empresaId,
            BodegaId bodegaId,
            DocumentoFuenteId documentoFuente,
            int totalLotes) {
        return new IngresoStockRegistradoEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                bodegaId,
                documentoFuente,
                totalLotes
        );
    }
}
