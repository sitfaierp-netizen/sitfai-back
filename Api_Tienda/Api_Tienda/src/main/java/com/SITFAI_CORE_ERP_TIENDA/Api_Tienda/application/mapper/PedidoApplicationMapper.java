package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.LineaPedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;

import java.util.List;
import java.util.Objects;

/**
 * Mapper estático de la Capa de Aplicación.
 * <p>
 * Transforma entidades y agregados del Dominio hacia DTOs de respuesta inmutables.
 * Sin dependencias de frameworks (REGLA-1, REGLA-2).
 */
public final class PedidoApplicationMapper {

    private PedidoApplicationMapper() {
        // Clase utilitaria estática
    }

    public static PedidoResponse toResponse(Pedido pedido) {
        Objects.requireNonNull(pedido, "PedidoApplicationMapper: pedido no puede ser null.");

        Dinero total = pedido.calcularTotal();
        List<LineaPedidoResponse> lineasResponse = pedido.getLineas().stream()
                .map(PedidoApplicationMapper::toLineaResponse)
                .toList();

        return new PedidoResponse(
                pedido.getId().valor(),
                pedido.getEmpresaId().valor(),
                pedido.getClienteId().valor(),
                pedido.getEstado().name(),
                total.monto(),
                total.moneda(),
                lineasResponse,
                pedido.getCreadoEn(),
                pedido.getActualizadoEn()
        );
    }

    public static LineaPedidoResponse toLineaResponse(LineaPedido linea) {
        Objects.requireNonNull(linea, "PedidoApplicationMapper: linea no puede ser null.");

        return new LineaPedidoResponse(
                linea.getId(),
                linea.getProductoId().valor(),
                linea.getCantidad(),
                linea.getPrecioUnitario().monto(),
                linea.getPrecioUnitario().moneda(),
                linea.subtotal().monto()
        );
    }
}
