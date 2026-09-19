package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Lote Revertido en una Devolución.
 * Contiene la cantidad específica de un producto y su lote asociado que retornará al stock físico.
 * Inmutable. Cumple con la regla CAJ-08.
 */
public record LoteRevertido(ProductoId productoId, String codigoLote, BigDecimal cantidad) {

    public LoteRevertido {
        Objects.requireNonNull(productoId, "El ProductoId es obligatorio en un lote revertido");
        Objects.requireNonNull(codigoLote, "El código de lote es obligatorio en un lote revertido");
        Objects.requireNonNull(cantidad, "La cantidad a revertir es obligatoria");
        
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad revertida debe ser mayor a cero");
        }
    }
}
