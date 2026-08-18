package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject;

/**
 * Value Object: Referencia al Documento Fuente que origina un MovimientoInventario.
 * <p>
 * BOD-04: No existe movimiento de stock sin un documento fuente (Orden de Compra,
 * Factura de Venta, Nota de Transferencia, Ajuste de Inventario, etc.).
 * Este VO garantiza trazabilidad completa de cada movimiento.
 * <p>
 * El {@code tipo} identifica la clase de documento (ej. "ORDEN_COMPRA", "VENTA", "AJUSTE").
 * El {@code numero} es el número o código único del documento dentro de la Empresa.
 * <p>
 * Reglas validadas: BOD-04, REGLA-3 (DDD — inmutabilidad), MCP-01.
 */
public record DocumentoFuenteId(String tipo, String numero) {

    /**
     * Constructor compacto — validación fail-fast.
     * Ambos campos son obligatorios para garantizar trazabilidad.
     */
    public DocumentoFuenteId {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException(
                    "DocumentoFuenteId: el tipo de documento no puede ser null ni vacío.");
        }
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException(
                    "DocumentoFuenteId: el número de documento no puede ser null ni vacío.");
        }
    }

    /**
     * Representación canónica del documento fuente para auditoría.
     * Formato: "TIPO#NUMERO" (ej. "ORDEN_COMPRA#OC-2026-00045")
     */
    @Override
    public String toString() {
        return tipo + "#" + numero;
    }
}
