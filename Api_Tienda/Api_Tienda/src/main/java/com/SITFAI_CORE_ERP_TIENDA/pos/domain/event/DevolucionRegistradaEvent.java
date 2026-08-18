package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DevolucionRegistradaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        SucursalId sucursalId,
        UUID ventaOrigenId, // Agregado para cumplir invariante tributaria (Factura Original)
        List<LineaDevolucion> lineas,
        Instant ocurridoEn
) implements DomainEvent {

    public record LineaDevolucion(UUID productoId, int cantidad, java.math.BigDecimal precioUnitario) {}

    public static DevolucionRegistradaEvent of(EmpresaId empresaId, SucursalId sucursalId, UUID ventaOrigenId, List<LineaDevolucion> lineas) {
        return new DevolucionRegistradaEvent(
                UUID.randomUUID(),
                empresaId,
                sucursalId,
                ventaOrigenId,
                List.copyOf(lineas),
                Instant.now()
        );
    }
}
