package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Emitido por el Agregado {@code Bodega} cuando el stock de un producto ha cambiado.
 * <p>
 * Inmutable por diseño (record Java 25).
 *
 * @param eventoId    Identificador único del evento (idempotencia).
 * @param bodegaId    Bodega donde el stock cambió.
 * @param productoId  Producto cuyo stock fue actualizado.
 * @param empresaId   Tenant (MT-01).
 * @param stockNuevo  Stock total del producto en la Bodega después del movimiento.
 * @param ocurridoEn  Timestamp del evento.
 */
public record StockActualizadoEvent(
        UUID eventoId,
        BodegaId bodegaId,
        ProductoId productoId,
        EmpresaId empresaId,
        BigDecimal stockNuevo,
        Instant ocurridoEn
) implements DomainEvent {

    /**
     * Factory method — genera un eventoId único al momento del evento.
     */
    public static StockActualizadoEvent of(
            BodegaId bodegaId,
            ProductoId productoId,
            EmpresaId empresaId,
            BigDecimal stockNuevo) {

        return new StockActualizadoEvent(
                UUID.randomUUID(),
                bodegaId,
                productoId,
                empresaId,
                stockNuevo,
                Instant.now()
        );
    }
}
