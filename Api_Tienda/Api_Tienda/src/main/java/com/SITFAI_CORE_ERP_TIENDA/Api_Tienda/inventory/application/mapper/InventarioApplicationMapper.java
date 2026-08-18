package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.StockResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.MovimientoInventario;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.math.BigDecimal;

/**
 * Mapper de la Capa de Aplicación: Dominio → DTOs de respuesta.
 * <p>
 * Clase utilitaria estática — sin estado, sin Spring (@Component no necesario aquí
 * ya que los Services lo usan directamente). Su única responsabilidad es transformar
 * objetos del Dominio a DTOs de respuesta de la capa de Aplicación.
 * <p>
 * ⚠️ NO realiza transformaciones HTTP (eso es responsabilidad del adaptador REST).
 * <p>
 * Reglas validadas: REGLA-1 (no mezclar capas), REGLA-2 (mapper en application/mapper),
 * REGLA-5 (nunca devolver objetos de dominio directamente — REGLA-5 aplica a REST,
 * pero adoptamos el mismo principio desde Application).
 */
public final class InventarioApplicationMapper {

    private InventarioApplicationMapper() {
        // Clase utilitaria — no instanciable
    }

    /**
     * Convierte el Agregado {@code Bodega} a su DTO de respuesta.
     *
     * @param bodega Agregado Bodega (estado post-creación o post-persistencia).
     * @return       {@code BodegaResponse} con los datos de la Bodega.
     */
    public static BodegaResponse toBodegaResponse(Bodega bodega) {
        return new BodegaResponse(
                bodega.getId().toString(),
                bodega.getEmpresaId().toString(),
                bodega.getSucursalId().toString(),
                bodega.getCodigo(),
                bodega.getNombre(),
                bodega.isActiva(),
                bodega.getCreadoEn()
        );
    }

    /**
     * Convierte la Entidad {@code MovimientoInventario} a su DTO de respuesta,
     * incluyendo el stock resultante del producto afectado tras el movimiento.
     *
     * @param movimiento      Entidad de movimiento registrada.
     * @param stockResultante Stock del producto en la Bodega DESPUÉS del movimiento.
     * @return                {@code MovimientoResponse} con todos los datos de confirmación.
     */
    public static MovimientoResponse toMovimientoResponse(
            MovimientoInventario movimiento,
            BigDecimal stockResultante) {

        return new MovimientoResponse(
                movimiento.getId().toString(),
                movimiento.getBodegaId().toString(),
                movimiento.getProductoId().toString(),
                movimiento.getTipo().name(),
                movimiento.getCantidad().valor(),
                stockResultante,
                movimiento.getDocumentoFuente().toString(),
                movimiento.getFechaRegistro()
        );
    }

    /**
     * Construye un {@code StockResponse} desde los datos de la Bodega.
     *
     * @param bodega     Bodega cuyo stock se consultó.
     * @param productoId Producto consultado.
     * @return           {@code StockResponse} con el stock actual.
     */
    public static StockResponse toStockResponse(Bodega bodega, ProductoId productoId) {
        BigDecimal stock = bodega.consultarStock(productoId);
        return new StockResponse(
                bodega.getId().toString(),
                productoId.toString(),
                stock
        );
    }
}
