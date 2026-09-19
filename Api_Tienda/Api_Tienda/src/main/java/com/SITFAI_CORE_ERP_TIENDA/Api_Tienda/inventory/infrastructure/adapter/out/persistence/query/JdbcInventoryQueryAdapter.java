package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.query;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.query.InventoryQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.MovimientoKardexView;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.StockDisponibleView;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class JdbcInventoryQueryAdapter implements InventoryQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcInventoryQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
    }

    @Override
    public List<StockDisponibleView> findStockByEmpresaAndBodega(String empresaId, String bodegaId) {
        String sql = """
            SELECT 
                BIN_TO_UUID(empresa_id) as empresa_id,
                BIN_TO_UUID(bodega_id) as bodega_id,
                BIN_TO_UUID(producto_id) as producto_id,
                cantidad_total,
                ultima_actualizacion
            FROM inventory_stock_view
            WHERE empresa_id = UUID_TO_BIN(:empresaId) 
              AND bodega_id = UUID_TO_BIN(:bodegaId)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresaId", empresaId)
                .addValue("bodegaId", bodegaId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new StockDisponibleView(
                rs.getString("empresa_id"),
                rs.getString("bodega_id"),
                rs.getString("producto_id"),
                rs.getBigDecimal("cantidad_total"),
                rs.getTimestamp("ultima_actualizacion").toInstant()
        ));
    }

    @Override
    public List<MovimientoKardexView> findKardexByEmpresaBodegaAndProducto(String empresaId, String bodegaId, String productoId) {
        String sql = """
            SELECT 
                id,
                empresa_id,
                bodega_id,
                producto_id,
                tipo,
                cantidad,
                doc_fuente_tipo,
                doc_fuente_numero,
                fecha_registro
            FROM inventory_movimiento
            WHERE empresa_id = :empresaId 
              AND bodega_id = :bodegaId
              AND producto_id = :productoId
            ORDER BY fecha_registro DESC
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresaId", empresaId)
                .addValue("bodegaId", bodegaId)
                .addValue("productoId", productoId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new MovimientoKardexView(
                rs.getString("id"),
                rs.getString("empresa_id"),
                rs.getString("bodega_id"),
                rs.getString("producto_id"),
                rs.getString("tipo"),
                rs.getBigDecimal("cantidad"),
                rs.getString("doc_fuente_tipo"),
                rs.getString("doc_fuente_numero"),
                rs.getTimestamp("fecha_registro").toInstant()
        ));
    }
}
