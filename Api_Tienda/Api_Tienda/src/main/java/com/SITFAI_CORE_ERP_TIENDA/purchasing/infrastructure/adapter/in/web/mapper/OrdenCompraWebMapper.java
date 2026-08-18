package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.mapper;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoOrdenCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearOrdenBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.RemoverLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto.AgregarLineaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto.CambiarEstadoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto.CrearOrdenWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto.LineaOrdenWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto.OrdenCompraWebResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class OrdenCompraWebMapper {

    private OrdenCompraWebMapper() {
    }

    public static CrearOrdenBorradorCommand toCommand(UUID empresaId, CrearOrdenWebRequest request) {
        Objects.requireNonNull(empresaId, "empresaId es obligatorio");
        Objects.requireNonNull(request, "request no puede ser null");
        Objects.requireNonNull(request.proveedorId(), "proveedorId es obligatorio");

        return new CrearOrdenBorradorCommand(empresaId, request.proveedorId());
    }

    public static AgregarLineaCommand toCommand(UUID ordenCompraId, UUID empresaId, AgregarLineaWebRequest request) {
        Objects.requireNonNull(ordenCompraId, "ordenCompraId es obligatorio");
        Objects.requireNonNull(empresaId, "empresaId es obligatorio");
        Objects.requireNonNull(request, "request no puede ser null");

        return new AgregarLineaCommand(
                ordenCompraId,
                empresaId,
                request.productoId(),
                request.cantidad() != null ? request.cantidad() : BigDecimal.ZERO,
                request.costoUnitario() != null ? request.costoUnitario() : BigDecimal.ZERO
        );
    }

    public static RemoverLineaCommand toRemoverCommand(UUID ordenCompraId, UUID empresaId, UUID lineaId) {
        return new RemoverLineaCommand(ordenCompraId, empresaId, lineaId);
    }

    public static CambiarEstadoOrdenCommand toCommand(UUID ordenCompraId, UUID empresaId, CambiarEstadoWebRequest request) {
        return new CambiarEstadoOrdenCommand(
                ordenCompraId,
                empresaId,
                request.estado(),
                request.motivo()
        );
    }

    public static OrdenCompraWebResponse toWebResponse(OrdenCompraResponse response) {
        if (response == null) {
            return null;
        }

        List<LineaOrdenWebResponse> lineas = response.lineas() == null
                ? List.of()
                : response.lineas().stream()
                .map(OrdenCompraWebMapper::toLineaWebResponse)
                .toList();

        BigDecimal total = response.costoTotal() != null ? response.costoTotal() : BigDecimal.ZERO;

        return new OrdenCompraWebResponse(
                response.id(),
                response.empresaId(),
                response.proveedorId(),
                response.estado(),
                total,
                "COP",
                lineas,
                response.fechaCreacion() != null ? java.time.Instant.parse(response.fechaCreacion()) : null,
                response.fechaCreacion() != null ? java.time.Instant.parse(response.fechaCreacion()) : null
        );
    }

    public static LineaOrdenWebResponse toLineaWebResponse(OrdenCompraResponse.LineaResponse linea) {
        if (linea == null) {
            return null;
        }
        return new LineaOrdenWebResponse(
                null,
                linea.productoId(),
                linea.cantidadSolicitada(),
                linea.costoUnitarioPactado(),
                linea.subtotal(),
                "COP"
        );
    }

    public static List<OrdenCompraWebResponse> toWebResponseList(List<OrdenCompraResponse> list) {
        if (list == null) {
            return List.of();
        }
        return list.stream()
                .map(OrdenCompraWebMapper::toWebResponse)
                .toList();
    }
}
