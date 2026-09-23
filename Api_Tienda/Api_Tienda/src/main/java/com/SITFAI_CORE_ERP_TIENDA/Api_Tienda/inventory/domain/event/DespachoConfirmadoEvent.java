package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Emitido cuando un Despacho es confirmado (Outbound Logistics).
 * <p>
 * Este evento es consumido asíncronamente para descontar definitivamente las unidades
 * de la reserva en la Bodega correspondiente (BOD-03, BOD-04, MT-01).
 * <p>
 * Regla 1 (Clean Architecture): Record inmutable de Java 21 libre de dependencias externas.
 */
public record DespachoConfirmadoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        BodegaId bodegaId,
        DespachoId despachoId,
        PedidoId pedidoId,
        List<LineaDespachoDetalle> lineas
) implements DomainEvent {

    public DespachoConfirmadoEvent {
        Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
        Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01)");
        Objects.requireNonNull(despachoId, "despachoId es obligatorio");
        Objects.requireNonNull(pedidoId, "pedidoId es obligatorio (BOD-04)");
        lineas = lineas != null ? Collections.unmodifiableList(lineas) : Collections.emptyList();
    }

    public record LineaDespachoDetalle(ProductoId productoId, Cantidad cantidad) {
        public LineaDespachoDetalle {
            Objects.requireNonNull(productoId, "productoId es obligatorio");
            Objects.requireNonNull(cantidad, "cantidad es obligatoria");
        }
    }

    public static DespachoConfirmadoEvent of(
            EmpresaId empresaId,
            BodegaId bodegaId,
            DespachoId despachoId,
            PedidoId pedidoId,
            List<LineaDespachoDetalle> lineas) {
        return new DespachoConfirmadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                bodegaId,
                despachoId,
                pedidoId,
                lineas
        );
    }

    public static DespachoConfirmadoEvent of(
            EmpresaId empresaId,
            DespachoId despachoId,
            PedidoId pedidoId,
            List<LineaDespachoDetalle> lineas) {
        return of(empresaId, null, despachoId, pedidoId, lineas);
    }

    public static DespachoConfirmadoEvent of(
            EmpresaId empresaId,
            DespachoId despachoId,
            PedidoId pedidoId) {
        return of(empresaId, null, despachoId, pedidoId, Collections.emptyList());
    }
}
