package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.UUID;

public record StockReservadoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        BodegaId bodegaId,
        ProductoId productoId,
        EmpresaId empresaId,
        Cantidad cantidadReservada,
        DocumentoFuenteId documentoFuente
) implements DomainEvent {

    public StockReservadoEvent {
        java.util.Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        java.util.Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
        java.util.Objects.requireNonNull(bodegaId, "bodegaId es obligatorio");
        java.util.Objects.requireNonNull(productoId, "productoId es obligatorio");
        java.util.Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01)");
        java.util.Objects.requireNonNull(cantidadReservada, "cantidadReservada es obligatoria");
        java.util.Objects.requireNonNull(documentoFuente, "documentoFuente es obligatorio");
    }

    public static StockReservadoEvent of(BodegaId bodegaId, ProductoId productoId, EmpresaId empresaId, Cantidad cantidadReservada, DocumentoFuenteId documentoFuente) {
        return new StockReservadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                bodegaId,
                productoId,
                empresaId,
                cantidadReservada,
                documentoFuente
        );
    }
}
