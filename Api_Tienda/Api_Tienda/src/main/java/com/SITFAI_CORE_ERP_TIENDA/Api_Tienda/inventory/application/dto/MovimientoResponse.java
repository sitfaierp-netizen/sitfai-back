package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de respuesta para la operación de registrar un movimiento de inventario.
 * <p>
 * Devuelto por {@code RegistrarMovimientoUseCase}. Contiene la confirmación
 * del movimiento registrado y el stock resultante del producto afectado.
 * <p>
 * Record inmutable (Java 25). Sin anotaciones Jackson.
 *
 * @param movimientoId    UUID del movimiento registrado.
 * @param bodegaId        UUID de la Bodega afectada.
 * @param productoId      UUID del Producto afectado.
 * @param tipo            "ENTRADA" o "SALIDA".
 * @param cantidad        Cantidad del movimiento.
 * @param stockResultante Stock del producto después del movimiento.
 * @param documentoFuente Referencia al documento fuente (formato "TIPO#NUMERO").
 * @param fechaRegistro   Timestamp del movimiento.
 */
public record MovimientoResponse(
        String movimientoId,
        String bodegaId,
        String productoId,
        String tipo,
        BigDecimal cantidad,
        BigDecimal stockResultante,
        String documentoFuente,
        Instant fechaRegistro
) {}
