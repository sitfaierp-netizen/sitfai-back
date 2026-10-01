package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output;

import java.util.UUID;

/**
 * Anti-corruption port used by Inventory to validate tenant ownership of
 * references managed by other bounded contexts.
 */
public interface InventoryReferenceOwnershipPort {

    boolean sucursalPerteneceAEmpresa(UUID sucursalId, UUID empresaId);

    boolean productoPerteneceAEmpresa(UUID productoId, UUID empresaId);
}
