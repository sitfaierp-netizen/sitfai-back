package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ProductoId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio Inmutable: {@code DiscrepanciaInventarioDetectadaEvent}.
 * <p>
 * Se emite cuando el Agregado {@link com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.ConteoCiclico}
 * finaliza y detecta diferencias entre las cantidades físicas contadas y las cantidades teóricas del sistema.
 * <p>
 * Regla MT-01: Encapsula {@link EmpresaId} para aislamiento multi-tenant en consumidores.
 * Regla REGLA-3: Record Java 21 inmutable con timestamp y colección inmutable de discrepancias.
 * Regla AUD-01: Provee trazabilidad de auditoría para posterior aprobación de ajustes contables/logísticos.
 */
public record DiscrepanciaInventarioDetectadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        ConteoId conteoId,
        BodegaId bodegaId,
        List<DiscrepanciaItem> discrepancias
) {

    public DiscrepanciaInventarioDetectadaEvent {
        Objects.requireNonNull(empresaId, "DiscrepanciaInventarioDetectadaEvent: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(conteoId, "DiscrepanciaInventarioDetectadaEvent: conteoId es obligatorio.");
        Objects.requireNonNull(bodegaId, "DiscrepanciaInventarioDetectadaEvent: bodegaId es obligatorio.");
        Objects.requireNonNull(discrepancias, "DiscrepanciaInventarioDetectadaEvent: la lista de discrepancias no puede ser nula.");
        if (discrepancias.isEmpty()) {
            throw new IllegalArgumentException("DiscrepanciaInventarioDetectadaEvent: debe contener al menos una discrepancia.");
        }
        discrepancias = Collections.unmodifiableList(new ArrayList<>(discrepancias));
        if (eventoId == null) {
            eventoId = UUID.randomUUID();
        }
        if (ocurridoEn == null) {
            ocurridoEn = Instant.now();
        }
    }

    public static DiscrepanciaInventarioDetectadaEvent of(
            EmpresaId empresaId,
            ConteoId conteoId,
            BodegaId bodegaId,
            List<DiscrepanciaItem> discrepancias) {
        return new DiscrepanciaInventarioDetectadaEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                conteoId,
                bodegaId,
                discrepancias
        );
    }

    /**
     * Value Object inmutable que representa la discrepancia detectada en una línea de producto.
     */
    public record DiscrepanciaItem(
            ProductoId productoId,
            int cantidadTeorica,
            int cantidadFisica,
            int diferencia
    ) {
        public DiscrepanciaItem {
            Objects.requireNonNull(productoId, "DiscrepanciaItem: productoId es obligatorio.");
            if (diferencia != (cantidadFisica - cantidadTeorica)) {
                throw new IllegalArgumentException(
                        "DiscrepanciaItem: la diferencia debe ser estrictamente (cantidadFisica - cantidadTeorica). " +
                        "Esperado: " + (cantidadFisica - cantidadTeorica) + ", Recibido: " + diferencia);
            }
        }

        public static DiscrepanciaItem de(ProductoId productoId, int cantidadTeorica, int cantidadFisica) {
            return new DiscrepanciaItem(productoId, cantidadTeorica, cantidadFisica, cantidadFisica - cantidadTeorica);
        }
    }
}
