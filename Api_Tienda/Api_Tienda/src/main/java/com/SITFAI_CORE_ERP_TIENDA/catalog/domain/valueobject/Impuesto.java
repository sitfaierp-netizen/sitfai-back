package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject;

/**
 * Tipo de impuesto aplicable a un Producto (normatividad DIAN Colombia).
 * Coherente con el Bounded Context billing.
 */
public enum Impuesto {
    IVA_0,
    IVA_5,
    IVA_19,
    EXCLUIDO,
    EXENTO
}
