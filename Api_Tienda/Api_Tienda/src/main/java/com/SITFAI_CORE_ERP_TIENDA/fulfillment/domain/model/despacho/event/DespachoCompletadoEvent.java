package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.event;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.PedidoId;

import java.time.LocalDateTime;
import java.util.Objects;

public record DespachoCompletadoEvent(
        EmpresaId empresaId,
        DespachoId despachoId,
        PedidoId pedidoId,
        BodegaId bodegaId,
        LocalDateTime fechaEmision
) {
    public DespachoCompletadoEvent {
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        Objects.requireNonNull(despachoId, "El despachoId no puede ser nulo");
        Objects.requireNonNull(pedidoId, "El pedidoId no puede ser nulo");
        Objects.requireNonNull(bodegaId, "El bodegaId no puede ser nulo");
        Objects.requireNonNull(fechaEmision, "La fechaEmision no puede ser nula");
    }

    public static DespachoCompletadoEvent de(EmpresaId empresaId, DespachoId despachoId, PedidoId pedidoId, BodegaId bodegaId) {
        return new DespachoCompletadoEvent(empresaId, despachoId, pedidoId, bodegaId, LocalDateTime.now());
    }
}
