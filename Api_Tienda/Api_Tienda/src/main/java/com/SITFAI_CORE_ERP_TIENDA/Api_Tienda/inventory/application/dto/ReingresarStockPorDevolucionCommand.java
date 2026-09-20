package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReingresarStockPorDevolucionCommand(
        UUID empresaId,
        UUID sucursalId,
        UUID ventaOrigenId,
        List<LoteRevertidoDto> lotesRevertidos
) {
    public record LoteRevertidoDto(
            UUID productoId,
            String codigoLote,
            BigDecimal cantidad
    ) {}
}
