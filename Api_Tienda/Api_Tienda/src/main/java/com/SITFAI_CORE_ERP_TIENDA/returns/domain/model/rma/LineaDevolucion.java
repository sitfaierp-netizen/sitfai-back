package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma;

import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.CantidadDevuelta;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.MotivoDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.ProductoId;

import java.util.UUID;

public class LineaDevolucion {
    private final UUID id;
    private final ProductoId productoId;
    private final CantidadDevuelta cantidadDevuelta;
    private final MotivoDevolucion motivoDevolucion;
    private EstadoInspeccion estadoInspeccion;

    public enum EstadoInspeccion {
        PENDIENTE, APROBADA, RECHAZADA
    }

    public LineaDevolucion(ProductoId productoId, CantidadDevuelta cantidadDevuelta, MotivoDevolucion motivoDevolucion) {
        this.id = UUID.randomUUID();
        this.productoId = productoId;
        this.cantidadDevuelta = cantidadDevuelta;
        this.motivoDevolucion = motivoDevolucion;
        this.estadoInspeccion = EstadoInspeccion.PENDIENTE;
    }

    public void inspeccionar(boolean aprobado) {
        if (this.estadoInspeccion != EstadoInspeccion.PENDIENTE) {
            throw new IllegalStateException("La línea ya fue inspeccionada");
        }
        this.estadoInspeccion = aprobado ? EstadoInspeccion.APROBADA : EstadoInspeccion.RECHAZADA;
    }

    public UUID getId() { return id; }
    public ProductoId getProductoId() { return productoId; }
    public CantidadDevuelta getCantidadDevuelta() { return cantidadDevuelta; }
    public MotivoDevolucion getMotivoDevolucion() { return motivoDevolucion; }
    public EstadoInspeccion getEstadoInspeccion() { return estadoInspeccion; }
}
