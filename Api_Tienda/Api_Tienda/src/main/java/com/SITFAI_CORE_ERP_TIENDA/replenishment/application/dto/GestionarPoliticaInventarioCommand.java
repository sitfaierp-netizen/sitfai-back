package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto;

import java.util.UUID;

public record GestionarPoliticaInventarioCommand(
        UUID politicaId,
        UUID empresaId,
        UUID bodegaId,
        UUID productoId,
        int puntoReorden,
        int nivelOptimo,
        boolean activa
) {
}
