package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.util.UUID;

/**
 * Entidad interna del Agregado Recepcion.
 */
public class LineaRecepcion {
    private final UUID id;
    private final ProductoId productoId;
    private final Cantidad cantidadRecibida;
    private final Lote lote;

    public LineaRecepcion(ProductoId productoId, Cantidad cantidadRecibida, Lote lote) {
        if (productoId == null) throw new IllegalArgumentException("La línea debe tener un ProductoId.");
        if (cantidadRecibida == null) throw new IllegalArgumentException("La línea debe tener una Cantidad.");
        if (lote == null) throw new IllegalArgumentException("La línea debe tener un Lote asignado.");
        
        this.id = UUID.randomUUID();
        this.productoId = productoId;
        this.cantidadRecibida = cantidadRecibida;
        this.lote = lote;
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public Cantidad getCantidadRecibida() {
        return cantidadRecibida;
    }

    public Lote getLote() {
        return lote;
    }
}
