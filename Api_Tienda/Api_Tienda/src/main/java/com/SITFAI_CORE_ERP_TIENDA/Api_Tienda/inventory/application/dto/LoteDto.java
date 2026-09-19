package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.time.LocalDate;

public record LoteDto(
        String codigoLote,
        LocalDate fechaCaducidad
) {
}
