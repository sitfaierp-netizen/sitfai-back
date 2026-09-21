package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.LineaPedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.RemoverLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.AgregarLineaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CancelarPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CrearPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.LineaPedidoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.PedidoWebResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Mapper Web: Transforma entre DTOs de transporte HTTP y Commands / Responses de la Capa de Aplicación.
 * <p>
 * Pertenece a la Capa de Infraestructura (REGLA-5).
 */
@Component
public class PedidoWebMapper {

    public CrearPedidoCommand toCommand(UUID empresaId, CrearPedidoWebRequest request) {
        Objects.requireNonNull(empresaId, "PedidoWebMapper: empresaId es requerido.");
        Objects.requireNonNull(request, "PedidoWebMapper: request es requerido.");

        List<CrearPedidoCommand.LineaComando> lineas = request.lineas() != null 
                ? request.lineas().stream().map(l -> new CrearPedidoCommand.LineaComando(l.productoId(), l.cantidad(), l.precioUnitario())).toList()
                : List.of();

        return new CrearPedidoCommand(empresaId, request.clienteId(), lineas);
    }

    public AgregarLineaPedidoCommand toAgregarLineaCommand(UUID empresaId, UUID pedidoId, AgregarLineaWebRequest request) {
        Objects.requireNonNull(empresaId, "PedidoWebMapper: empresaId es requerido.");
        Objects.requireNonNull(pedidoId, "PedidoWebMapper: pedidoId es requerido.");
        Objects.requireNonNull(request, "PedidoWebMapper: request es requerido.");

        return new AgregarLineaPedidoCommand(
                empresaId,
                pedidoId,
                request.productoId(),
                request.cantidad(),
                request.precioUnitario(),
                request.moneda()
        );
    }

    public AgregarLineaCommand toCommand(UUID empresaId, UUID pedidoId, AgregarLineaWebRequest request) {
        Objects.requireNonNull(empresaId, "PedidoWebMapper: empresaId es requerido.");
        Objects.requireNonNull(pedidoId, "PedidoWebMapper: pedidoId es requerido.");
        Objects.requireNonNull(request, "PedidoWebMapper: request es requerido.");

        return new AgregarLineaCommand(
                empresaId,
                pedidoId,
                request.productoId(),
                request.cantidad(),
                request.precioUnitario(),
                request.moneda()
        );
    }

    public RemoverLineaPedidoCommand toRemoverLineaCommand(UUID empresaId, UUID pedidoId, UUID lineaId) {
        Objects.requireNonNull(empresaId, "PedidoWebMapper: empresaId es requerido.");
        Objects.requireNonNull(pedidoId, "PedidoWebMapper: pedidoId es requerido.");
        Objects.requireNonNull(lineaId, "PedidoWebMapper: lineaId es requerido.");

        return new RemoverLineaPedidoCommand(empresaId, pedidoId, lineaId);
    }

    public ConfirmarPedidoCommand toConfirmarCommand(UUID empresaId, UUID pedidoId) {
        return new ConfirmarPedidoCommand(empresaId, pedidoId);
    }

    public CancelarPedidoCommand toCancelarCommand(UUID empresaId, UUID pedidoId, CancelarPedidoWebRequest request) {
        String motivo = request != null ? request.motivo() : null;
        return new CancelarPedidoCommand(empresaId, pedidoId, motivo);
    }

    public PedidoWebResponse toWebResponse(PedidoResponse response) {
        if (response == null) {
            return null;
        }

        List<LineaPedidoWebResponse> lineasWeb = response.lineas() != null
                ? response.lineas().stream().map(this::toLineaWebResponse).toList()
                : List.of();

        return new PedidoWebResponse(
                response.id(),
                response.empresaId(),
                response.clienteId(),
                response.estado(),
                response.total(),
                response.moneda(),
                lineasWeb,
                response.creadoEn(),
                response.actualizadoEn()
        );
    }

    public List<PedidoWebResponse> toWebResponseList(List<PedidoResponse> responses) {
        if (responses == null) {
            return List.of();
        }
        return responses.stream()
                .map(this::toWebResponse)
                .toList();
    }

    private LineaPedidoWebResponse toLineaWebResponse(LineaPedidoResponse linea) {
        return new LineaPedidoWebResponse(
                linea.id(),
                linea.productoId(),
                linea.cantidad(),
                linea.precioUnitario(),
                linea.moneda(),
                linea.subtotal()
        );
    }
}
