package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo;

/**
 * Fases logísticas del ciclo de vida de un Despacho en Logística de Salida (Outbound Logistics).
 */
public enum EstadoDespacho {
    PENDIENTE,
    EN_PICKING,
    EMPACADO,
    DESPACHADO
}
