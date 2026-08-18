package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evento emitido cuando una Orden de Compra cambia al estado RECIBIDA.
 * Este evento es consumido por Inventory (putaway).
 */
public record OrdenCompraRecibidaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        OrdenCompraId ordenCompraId,
        List<LineaRecibida> lineas,
        Instant ocurridoEn
) implements DomainEvent {

    public record LineaRecibida(UUID productoId, java.math.BigDecimal cantidad) {}

    public static OrdenCompraRecibidaEvent of(EmpresaId empresaId, OrdenCompraId ordenCompraId, List<LineaRecibida> lineas) {
        return new OrdenCompraRecibidaEvent(
                UUID.randomUUID(),
                empresaId,
                ordenCompraId,
                List.copyOf(lineas),
                Instant.now()
        );
    }
}
