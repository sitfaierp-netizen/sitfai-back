package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.query;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ProductoMetricaSalidaDto;

import java.util.List;
import java.util.UUID;

/**
 * Driven Output Port: Puerto de consulta para obtener métricas históricas de salida
 * de productos en una bodega para el motor de Análisis ABC.
 * <p>
 * Regla MT-01: Exige empresaId para garantizar aislamiento multi-tenant.
 */
public interface MetricaMovimientoQueryPort {

    /**
     * Obtiene las métricas consolidadas de salidas de todos los productos en una bodega.
     *
     * @param empresaId Identificador de la empresa (tenant).
     * @param bodegaId  Identificador de la bodega.
     * @return Lista de métricas por producto.
     */
    List<ProductoMetricaSalidaDto> obtenerMetricasSalidaPorBodega(UUID empresaId, UUID bodegaId);
}
