package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Emitido por el Agregado {@code Bodega} cuando se descuenta stock
 * exitosamente por una venta física (POS) aplicando el algoritmo FEFO (First Expires, First Out).
 * <p>
 * Reglas de negocio validadas:
 * <ul>
 *   <li>BOD-04: Obligatoriedad de documento fuente para trazabilidad transaccional.</li>
 *   <li>BOD-05: Invariante de stock no negativo garantizada previo a la emisión.</li>
 *   <li>MT-01: Aislamiento multitenant garantizado por {@code EmpresaId}.</li>
 *   <li>INV-01: Trazabilidad del algoritmo FEFO.</li>
 *   <li>REGLA-1: Clean Architecture, record inmutable de Java 21 sin dependencias de frameworks.</li>
 *   <li>REGLA-3: Domain Event inmutable que implementa la interfaz sellada {@code DomainEvent}.</li>
 * </ul>
 *
 * @param eventoId        Identificador único del evento para idempotencia.
 * @param ocurridoEn      Marca de tiempo del suceso.
 * @param empresaId       Tenant al que pertenece la operación (MT-01).
 * @param bodegaId        Bodega donde se dedujo el stock.
 * @param productoId      Producto vendido.
 * @param cantidad        Cantidad total deducida.
 * @param documentoFuente Documento fuente que originó la venta (transacción POS).
 */
public record StockDescontadoPorVentaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        BodegaId bodegaId,
        ProductoId productoId,
        Cantidad cantidad,
        DocumentoFuenteId documentoFuente
) implements DomainEvent {

    public StockDescontadoPorVentaEvent {
        Objects.requireNonNull(eventoId, "StockDescontadoPorVentaEvent: eventoId es obligatorio.");
        Objects.requireNonNull(ocurridoEn, "StockDescontadoPorVentaEvent: ocurridoEn es obligatorio.");
        Objects.requireNonNull(empresaId, "StockDescontadoPorVentaEvent: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(bodegaId, "StockDescontadoPorVentaEvent: bodegaId es obligatorio.");
        Objects.requireNonNull(productoId, "StockDescontadoPorVentaEvent: productoId es obligatorio.");
        Objects.requireNonNull(cantidad, "StockDescontadoPorVentaEvent: cantidad es obligatoria.");
        Objects.requireNonNull(documentoFuente, "StockDescontadoPorVentaEvent: documentoFuente es obligatorio (BOD-04).");
    }

    public static StockDescontadoPorVentaEvent of(
            EmpresaId empresaId,
            BodegaId bodegaId,
            ProductoId productoId,
            Cantidad cantidad,
            DocumentoFuenteId documentoFuente) {
        return new StockDescontadoPorVentaEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                bodegaId,
                productoId,
                cantidad,
                documentoFuente
        );
    }
}
