package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProcesarDevolucionWebRequest(
        UUID ticketOriginalId,
        BigDecimal montoDevuelto,
        List<LineaDevolucionWebRequest> lineas,
        List<LoteRevertidoWebRequest> lotesRevertidos
) {
    public record LineaDevolucionWebRequest(
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario
    ) {}

    public record LoteRevertidoWebRequest(
            UUID productoId,
            String codigoLote,
            BigDecimal cantidad
    ) {}
}
