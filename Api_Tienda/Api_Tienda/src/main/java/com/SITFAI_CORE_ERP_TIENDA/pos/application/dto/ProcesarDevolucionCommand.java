package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProcesarDevolucionCommand(
        UUID turnoId,
        UUID ticketOriginalId,
        BigDecimal montoDevuelto,
        List<LineaDevolucionDto> lineas,
        List<LoteRevertidoDto> lotesRevertidos
) {}
