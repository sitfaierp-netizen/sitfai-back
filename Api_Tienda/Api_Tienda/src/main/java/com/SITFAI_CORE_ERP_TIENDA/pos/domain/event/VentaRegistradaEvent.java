package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VentaRegistradaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        UUID turnoId,
        UUID cajaId,
        String clienteNit,
        String documentoFuenteId,
        List<LineaVenta> lineas,
        Instant ocurridoEn
) implements DomainEvent {

    public record LineaVenta(UUID productoId, int cantidad, java.math.BigDecimal precioUnitario) {}

    public static VentaRegistradaEvent of(EmpresaId empresaId, UUID turnoId, UUID cajaId, String clienteNit, String documentoFuenteId, List<LineaVenta> lineas) {
        return new VentaRegistradaEvent(
                UUID.randomUUID(),
                empresaId,
                turnoId,
                cajaId,
                clienteNit,
                documentoFuenteId,
                List.copyOf(lineas),
                Instant.now()
        );
    }
}
