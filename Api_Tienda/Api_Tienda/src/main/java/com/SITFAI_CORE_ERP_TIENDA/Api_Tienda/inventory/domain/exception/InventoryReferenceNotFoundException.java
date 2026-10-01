package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception;

/**
 * Generic not-found error for a cross-context reference outside the current
 * tenant. It deliberately omits foreign identifiers to prevent enumeration.
 */
public class InventoryReferenceNotFoundException extends DomainException {

    public InventoryReferenceNotFoundException() {
        super("INV-404", "Recurso relacionado no encontrado para el tenant autenticado.");
    }
}
