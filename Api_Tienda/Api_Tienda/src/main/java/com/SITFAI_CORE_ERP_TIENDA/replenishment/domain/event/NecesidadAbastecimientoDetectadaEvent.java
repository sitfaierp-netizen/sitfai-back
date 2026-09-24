package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.event;

import java.time.Instant;
import java.util.UUID;

public record NecesidadAbastecimientoDetectadaEvent(
        UUID id,
        Instant ocurridoEn,
        UUID empresaId,
        UUID bodegaId,
        UUID productoId,
        int cantidadAReponer
) {
    public NecesidadAbastecimientoDetectadaEvent {
        if (id == null) id = UUID.randomUUID();
        if (ocurridoEn == null) ocurridoEn = Instant.now();
    }
}
