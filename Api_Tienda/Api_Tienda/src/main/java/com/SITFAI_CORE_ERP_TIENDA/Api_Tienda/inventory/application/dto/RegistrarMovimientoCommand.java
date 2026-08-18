package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

/**
 * Comando para registrar un Movimiento de Inventario (ENTRADA o SALIDA) en una Bodega.
 * <p>
 * Record puro — inmutable (Java 25). Sin anotaciones web ni Jackson.
 * <p>
 * El {@code tipo} debe ser el nombre textual del enum: "ENTRADA" o "SALIDA".
 * La conversión desde el payload HTTP se realiza en el adaptador REST (Infraestructura).
 * <p>
 * Si el movimiento es de SALIDA y deja el stock negativo, el Dominio lanzará
 * {@code StockInsuficienteException} — la Application Layer no captura esa excepción,
 * la deja fluir hacia el adaptador REST para su traducción a RFC 7807.
 * <p>
 * Reglas validadas: BOD-03, BOD-04, BOD-05, MT-01, REGLA-2.
 *
 * @param empresaId       UUID del tenant — extraído del JWT (MT-01).
 * @param bodegaId        UUID de la Bodega donde se registra el movimiento.
 * @param productoId      UUID del Producto afectado.
 * @param cantidad        Cantidad de unidades (debe ser estrictamente positiva).
 * @param tipo            "ENTRADA" o "SALIDA".
 * @param docFuenteTipo   Tipo del documento fuente (ej. "ORDEN_COMPRA", "VENTA") — BOD-04.
 * @param docFuenteNumero Número del documento fuente (ej. "OC-2026-00045") — BOD-04.
 */
public record RegistrarMovimientoCommand(
        String empresaId,
        String bodegaId,
        String productoId,
        java.math.BigDecimal cantidad,
        String tipo,
        String docFuenteTipo,
        String docFuenteNumero
) {
    public RegistrarMovimientoCommand {
        if (empresaId == null || empresaId.isBlank())
            throw new IllegalArgumentException("RegistrarMovimientoCommand: empresaId es obligatorio (MT-01).");
        if (bodegaId == null || bodegaId.isBlank())
            throw new IllegalArgumentException("RegistrarMovimientoCommand: bodegaId es obligatorio.");
        if (productoId == null || productoId.isBlank())
            throw new IllegalArgumentException("RegistrarMovimientoCommand: productoId es obligatorio.");
        if (cantidad == null || cantidad.compareTo(java.math.BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("RegistrarMovimientoCommand: cantidad debe ser estrictamente positiva.");
        if (tipo == null || tipo.isBlank())
            throw new IllegalArgumentException("RegistrarMovimientoCommand: tipo es obligatorio (ENTRADA|SALIDA).");
        if (docFuenteTipo == null || docFuenteTipo.isBlank())
            throw new IllegalArgumentException("RegistrarMovimientoCommand: docFuenteTipo es obligatorio (BOD-04).");
        if (docFuenteNumero == null || docFuenteNumero.isBlank())
            throw new IllegalArgumentException("RegistrarMovimientoCommand: docFuenteNumero es obligatorio (BOD-04).");
    }
}
