package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo;

/**
 * Enum de Categorías del Análisis ABC de Inventario.
 * <p>
 * El Análisis ABC clasifica los productos según su contribución relativa
 * al valor total de los movimientos de la bodega en un período dado:
 * <ul>
 *   <li><b>A</b>: Productos de Alto Valor / Alta Rotación (~20% del catálogo, ~80% del valor — Principio de Pareto).
 *       Requieren control estricto, revisión periódica frecuente y stock de seguridad elevado.</li>
 *   <li><b>B</b>: Productos de Valor Medio / Rotación Media (~30% del catálogo, ~15% del valor).
 *       Control moderado, revisión periódica estándar.</li>
 *   <li><b>C</b>: Productos de Bajo Valor / Baja Rotación (~50% del catálogo, ~5% del valor).
 *       Control simplificado, revisión periódica infrecuente.</li>
 *   <li><b>NO_CLASIFICADO</b>: Estado inicial. El producto aún no ha sido evaluado por el motor ABC
 *       o no tiene movimientos registrados en el período de análisis.</li>
 * </ul>
 * <p>
 * Regla REGLA-1: Enum puro de dominio — cero dependencias de frameworks.
 */
public enum CategoriaABC {

    /**
     * Alto valor / Alta rotación. Control crítico.
     */
    A,

    /**
     * Valor medio / Rotación media. Control estándar.
     */
    B,

    /**
     * Bajo valor / Baja rotación. Control simplificado.
     */
    C,

    /**
     * Sin clasificación. Estado inicial del Agregado antes del primer análisis.
     */
    NO_CLASIFICADO;

    /**
     * Indica si la categoría es una clasificación activa (no el estado inicial).
     */
    public boolean estaClasificado() {
        return this != NO_CLASIFICADO;
    }
}
