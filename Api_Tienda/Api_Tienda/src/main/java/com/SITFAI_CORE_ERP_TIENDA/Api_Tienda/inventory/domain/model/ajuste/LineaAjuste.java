package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad Interna: Representa una línea de detalle dentro de un Ajuste de Inventario.
 */
public class LineaAjuste {
    private final UUID id;
    private final ProductoId productoId;
    private final Lote lote; // Opcional, si el ajuste aplica a un lote específico
    private final BigDecimal diferencia;

    public LineaAjuste(ProductoId productoId, Lote lote, BigDecimal diferencia) {
        if (productoId == null) {
            throw new IllegalArgumentException("LineaAjuste: el productoId es obligatorio.");
        }
        if (diferencia == null || diferencia.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("LineaAjuste: la diferencia no puede ser nula ni cero.");
        }

        this.id = UUID.randomUUID();
        this.productoId = productoId;
        this.lote = lote;
        this.diferencia = diferencia;
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public Lote getLote() {
        return lote;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }
}
