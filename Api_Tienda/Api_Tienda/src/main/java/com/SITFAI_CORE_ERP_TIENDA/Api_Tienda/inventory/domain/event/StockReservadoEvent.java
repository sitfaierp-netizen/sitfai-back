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
