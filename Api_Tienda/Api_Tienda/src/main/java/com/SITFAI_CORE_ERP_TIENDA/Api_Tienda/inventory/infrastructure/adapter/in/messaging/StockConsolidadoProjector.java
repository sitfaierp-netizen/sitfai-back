package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockActualizadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Proyector CQRS: Escucha los eventos del modelo de escritura de inventario
 * y actualiza de manera desnormalizada la tabla de lectura.
 */
@Component
public class StockConsolidadoProjector {

    private static final Logger log = LoggerFactory.getLogger(StockConsolidadoProjector.class);
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StockConsolidadoProjector(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Reacciona de forma asíncrona al evento de actualización de stock.
     */
    @Async
    @EventListener
    @Transactional
    public void onStockActualizado(StockActualizadoEvent event) {
        log.debug("Proyectando stock para producto={}, bodega={}, empresa={}",
                event.productoId().valor(), event.bodegaId().valor(), event.empresaId().valor());

        String sql = """
            INSERT INTO inventory_stock_view (empresa_id, bodega_id, producto_id, cantidad_total, ultima_actualizacion)
            VALUES (UUID_TO_BIN(:empresaId), UUID_TO_BIN(:bodegaId), UUID_TO_BIN(:productoId), :cantidad, :fecha)
            ON DUPLICATE KEY UPDATE 
                cantidad_total = VALUES(cantidad_total),
                ultima_actualizacion = VALUES(ultima_actualizacion)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresaId", event.empresaId().valor().toString())
                .addValue("bodegaId", event.bodegaId().valor().toString())
                .addValue("productoId", event.productoId().valor().toString())
                .addValue("cantidad", event.stockNuevo())
                .addValue("fecha", java.sql.Timestamp.from(event.ocurridoEn()));

        jdbcTemplate.update(sql, params);
        log.info("Proyección CQRS de stock actualizada correctamente.");
    }
}
