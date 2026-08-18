package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model;

/**
 * Enum: Clasificación del propósito y estado de la Bodega.
 * Permite segmentar operaciones logísticas como ventas, retención de mercancía
 * dañada o procesos de cuarentena técnica.
 */
public enum TipoBodega {
    /** Bodega estándar para productos disponibles para venta o distribución. */
    VENTA,
    
    /** Bodega para productos en revisión de calidad, devoluciones pendientes o sospecha de defectos. */
    CUARENTENA,
    
    /** Bodega para productos definitivamente defectuosos, vencidos o destruidos. */
    MERMA
}
