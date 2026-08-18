package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConsultarStockQuery;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CrearBodegaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.StockResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.BodegaWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.CrearBodegaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.MovimientoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarMovimientoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.StockWebResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper Web: Transforma entre Web DTOs (HTTP) y Commands/Queries/Responses de la Capa de Aplicación.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-2, REGLA-5).
 */
@Component
public class InventarioWebMapper {

    public CrearBodegaCommand toCommand(String empresaId, CrearBodegaWebRequest request) {
        return new CrearBodegaCommand(
                empresaId,
                request.sucursalId(),
                request.codigo(),
                request.nombre()
        );
    }

    public RegistrarMovimientoCommand toCommand(String empresaId, String bodegaId, RegistrarMovimientoWebRequest request) {
        return new RegistrarMovimientoCommand(
                empresaId,
                bodegaId,
                request.productoId(),
                request.cantidad(),
                request.tipo(),
                request.docFuenteTipo(),
                request.docFuenteNumero()
        );
    }

    public ConsultarStockQuery toQuery(String empresaId, String bodegaId, String productoId) {
        return new ConsultarStockQuery(
                empresaId,
                bodegaId,
                productoId
        );
    }

    public BodegaWebResponse toWebResponse(BodegaResponse response) {
        return new BodegaWebResponse(
                response.bodegaId(),
                response.empresaId(),
                response.sucursalId(),
                response.codigo(),
                response.nombre(),
                response.activa(),
                response.creadoEn()
        );
    }

    public MovimientoWebResponse toWebResponse(MovimientoResponse response) {
        return new MovimientoWebResponse(
                response.movimientoId(),
                response.bodegaId(),
                response.productoId(),
                response.tipo(),
                response.cantidad(),
                response.stockResultante(),
                response.documentoFuente(),
                response.fechaRegistro()
        );
    }

    public StockWebResponse toWebResponse(StockResponse response) {
        return new StockWebResponse(
                response.bodegaId(),
                response.productoId(),
                response.stock()
        );
    }
}
