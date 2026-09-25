package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.CantidadFisica;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ProductoId;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad Local: Representa una línea o ítem de detalle dentro del Agregado {@link ConteoCiclico}.
 * <p>
 * Encapsula el producto evaluado, la cantidad teórica esperada por el sistema y la
 * cantidad física efectivamente contada en la bodega por el auditor o inventarista.
 * <p>
 * Regla REGLA-3: Entidad interna de agregado; su ciclo de vida y modificaciones
 * están estrictamente gobernados por el Aggregate Root {@link ConteoCiclico}.
 * Regla REGLA-1: Cero dependencias de JPA o frameworks de persistencia en dominio.
 */
public class DetalleConteo {

    private final UUID id;
    private final ProductoId productoId;
    private final int cantidadTeorica;
    private CantidadFisica cantidadFisica;

    public DetalleConteo(UUID id, ProductoId productoId, int cantidadTeorica, CantidadFisica cantidadFisica) {
        this.id = id != null ? id : UUID.randomUUID();
        this.productoId = Objects.requireNonNull(productoId, "DetalleConteo: productoId no puede ser nulo.");
        if (cantidadTeorica < 0) {
            throw new IllegalArgumentException("DetalleConteo: cantidadTeorica no puede ser negativa. Recibido: " + cantidadTeorica);
        }
        this.cantidadTeorica = cantidadTeorica;
        this.cantidadFisica = cantidadFisica; // Inicialmente puede ser nula antes de conteo físico
    }

    public DetalleConteo(ProductoId productoId, int cantidadTeorica) {
        this(UUID.randomUUID(), productoId, cantidadTeorica, null);
    }

    /**
     * Registra o actualiza la cantidad física constatada en el almacén.
     *
     * @param cantidad Cantidad física contada (>= 0).
     */
    void registrarConteoFisico(CantidadFisica cantidad) {
        this.cantidadFisica = Objects.requireNonNull(cantidad, "DetalleConteo: cantidadFisica no puede ser nula al registrar conteo.");
    }

    /**
     * Evalúa si existe discrepancia entre la cantidad física reportada y la teórica del sistema.
     * Si aún no ha sido contado, no se considera discrepancia concluida.
     */
    public boolean tieneDiscrepancia() {
        return cantidadFisica != null && cantidadFisica.valor() != cantidadTeorica;
    }

    /**
     * Calcula la diferencia matemática: (Física - Teórica).
     * Un valor positivo indica sobrante, un valor negativo indica faltante.
     */
    public int calcularDiferencia() {
        return cantidadFisica != null ? cantidadFisica.valor() - cantidadTeorica : 0;
    }

    public boolean fueContado() {
        return cantidadFisica != null;
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public int getCantidadTeorica() {
        return cantidadTeorica;
    }

    public CantidadFisica getCantidadFisica() {
        return cantidadFisica;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleConteo that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId);
    }

    @Override
    public String toString() {
        return "DetalleConteo{" +
                "productoId=" + productoId +
                ", cantidadTeorica=" + cantidadTeorica +
                ", cantidadFisica=" + cantidadFisica +
                '}';
    }
}
