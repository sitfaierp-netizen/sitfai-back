package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.cqrs;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite ID para StockProyeccionJpaEntity
 */
public class StockProyeccionId implements Serializable {
    private UUID empresaId;
    private UUID bodegaId;
    private UUID productoId;

    public StockProyeccionId() {}

    public StockProyeccionId(UUID empresaId, UUID bodegaId, UUID productoId) {
        this.empresaId = empresaId;
        this.bodegaId = bodegaId;
        this.productoId = productoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockProyeccionId that = (StockProyeccionId) o;
        return Objects.equals(empresaId, that.empresaId) &&
                Objects.equals(bodegaId, that.bodegaId) &&
                Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empresaId, bodegaId, productoId);
    }
}
