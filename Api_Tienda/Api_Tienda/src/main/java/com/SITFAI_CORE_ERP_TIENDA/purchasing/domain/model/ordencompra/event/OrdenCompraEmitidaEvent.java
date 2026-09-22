package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProveedorId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Integración y Dominio inmutable: {@code OrdenCompraEmitidaEvent} (BOD-04, MT-01).
 * <p>
 * Emitido cuando una Orden de Compra es enviada al proveedor.
 * Notifica a la Bodega sobre el stock en tránsito para su posterior recepción y trazabilidad documental.
 */
public record OrdenCompraEmitidaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        OrdenCompraId ordenCompraId,
        ProveedorId proveedorId,
        int totalLineas,
        BigDecimal montoTotalEsperado,
        Instant ocurridoEn
) implements DomainEvent {

    public OrdenCompraEmitidaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser nulo");
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo (MT-01)");
        Objects.requireNonNull(ordenCompraId, "ordenCompraId no puede ser nulo");
        Objects.requireNonNull(proveedorId, "proveedorId no puede ser nulo");
        Objects.requireNonNull(montoTotalEsperado, "montoTotalEsperado no puede ser nulo");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser nulo");
    }

    public static OrdenCompraEmitidaEvent crear(
            EmpresaId empresaId,
            OrdenCompraId ordenCompraId,
            ProveedorId proveedorId,
            int totalLineas,
            BigDecimal montoTotalEsperado
    ) {
        return new OrdenCompraEmitidaEvent(
                UUID.randomUUID(),
                empresaId,
                ordenCompraId,
                proveedorId,
                totalLineas,
                montoTotalEsperado,
                Instant.now()
        );
    }
}
