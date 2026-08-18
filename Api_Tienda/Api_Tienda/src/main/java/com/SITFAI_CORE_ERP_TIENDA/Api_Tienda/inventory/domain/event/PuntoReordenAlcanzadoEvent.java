package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de Dominio: Se emite cuando el stock de un producto cruza el umbral
 * de su punto de reorden hacia abajo (BOD-08).
 */
public record PuntoReordenAlcanzadoEvent(
        UUID eventoId,
        EmpresaId empresaId,
        BodegaId bodegaId,
        ProductoId productoId,
        BigDecimal cantidadActual,
        Instant ocurridoEn
) implements DomainEvent {

    public static PuntoReordenAlcanzadoEvent of(
            EmpresaId empresaId,
            BodegaId bodegaId,
            ProductoId productoId,
            BigDecimal cantidadActual) {
        return new PuntoReordenAlcanzadoEvent(
                UUID.randomUUID(),
                empresaId,
                bodegaId,
                productoId,
                cantidadActual,
                Instant.now()
        );
    }
}
