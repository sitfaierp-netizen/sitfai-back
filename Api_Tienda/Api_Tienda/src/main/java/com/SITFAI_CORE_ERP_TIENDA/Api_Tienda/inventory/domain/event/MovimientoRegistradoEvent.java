package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoMovimiento;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Emitido por el Agregado {@code Bodega} cuando se registra exitosamente
 * un {@code MovimientoInventario} (ENTRADA o SALIDA).
 * <p>
 * Nomenclatura: {@code {Entidad}{AcciónPasado}Event} — REGLA-3 (architecture-rules.md).
 * Inmutable por diseño (record Java 25).
 * <p>
 * Consumidores potenciales:
 * <ul>
 *   <li>Módulo de Auditoría (Event Store — AUD-03).</li>
 *   <li>Módulo de Compras (confirmación de recepción).</li>
 *   <li>Módulo de Ventas (confirmación de despacho).</li>
 * </ul>
 * <p>
 * Reglas validadas: REGLA-3 (Domain Events), AUD-03, BOD-03, BOD-04, MCP-05.
 *
 * @param eventoId        Identificador único del evento (idempotencia).
 * @param bodegaId        Bodega donde ocurrió el movimiento.
 * @param productoId      Producto afectado.
 * @param empresaId       Tenant (MT-01).
 * @param tipo            ENTRADA o SALIDA.
 * @param cantidad        Cantidad del movimiento.
 * @param documentoFuente Documento origen que justificó el movimiento (BOD-04).
 * @param ocurridoEn      Timestamp del evento.
 */
public record MovimientoRegistradoEvent(
        UUID eventoId,
        BodegaId bodegaId,
        ProductoId productoId,
        EmpresaId empresaId,
        TipoMovimiento tipo,
        Cantidad cantidad,
        DocumentoFuenteId documentoFuente,
        Instant ocurridoEn
) implements DomainEvent {

    /**
     * Factory method — genera un eventoId único al momento del evento.
     */
    public static MovimientoRegistradoEvent of(
            BodegaId bodegaId,
            ProductoId productoId,
            EmpresaId empresaId,
            TipoMovimiento tipo,
            Cantidad cantidad,
            DocumentoFuenteId documentoFuente) {

        return new MovimientoRegistradoEvent(
                UUID.randomUUID(),
                bodegaId,
                productoId,
                empresaId,
                tipo,
                cantidad,
                documentoFuente,
                Instant.now()
        );
    }
}
