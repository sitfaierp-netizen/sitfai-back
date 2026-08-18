package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model;

/**
 * Enum: Estados del ciclo de vida de una Factura Electrónica (DIAN).
 */
public enum EstadoFacturaDian {
    BORRADOR,
    FIRMADA,
    ENVIADA_DIAN,
    APROBADA_DIAN,
    RECHAZADA_DIAN,
    ANULADA
}
