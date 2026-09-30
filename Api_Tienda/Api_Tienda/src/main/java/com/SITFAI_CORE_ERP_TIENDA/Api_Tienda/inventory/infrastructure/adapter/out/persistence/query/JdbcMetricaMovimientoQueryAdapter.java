package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.query;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ProductoMetricaSalidaDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.query.MetricaMovimientoQueryPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Driven Adapter: Implementa {@link MetricaMovimientoQueryPort} mediante JDBC directo.
 * <p>
 * Regla MT-01: Filtro estricto por empresaId.
 * Regla MONEY-01: Manejo con BigDecimal y escala fija.
 */
@Repository
public class JdbcMetricaMovimientoQueryAdapter implements MetricaMovimientoQueryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcMetricaMovimientoQueryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "NamedParameterJdbcTemplate no puede ser nulo");
    }

    @Override
    public List<ProductoMetricaSalidaDto> obtenerMetricasSalidaPorBodega(UUID empresaId, UUID bodegaId) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo");
        Objects.requireNonNull(bodegaId, "bodegaId no puede ser nulo");

        String sql = """
            SELECT 
                p_id AS producto_id,
                CAST(COALESCE(SUM(frecuencia), 0) AS SIGNED) AS frecuencia_salida,
                COALESCE(SUM(valor), 0.0000) AS valor_total_despachado
            FROM (
                SELECT 
                    m.producto_id AS p_id,
                    1 AS frecuencia,
                    (ABS(m.cantidad) * COALESCE(p.precio_venta, 1.0000)) AS valor
                FROM inventory_movimiento m
                LEFT JOIN catalog_productos p 
                    ON m.producto_id = p.id AND m.empresa_id = p.empresa_id
                WHERE m.empresa_id = :empresaId
                  AND m.bodega_id = :bodegaId
                  AND m.tipo = 'SALIDA'
                UNION ALL
                SELECT 
                    s.producto_id AS p_id,
                    0 AS frecuencia,
                    0.0000 AS valor
                FROM inventory_bodega_lote s
                JOIN inventory_bodega b 
                    ON s.bodega_id = b.id AND b.empresa_id = :empresaId
                WHERE s.bodega_id = :bodegaId
            ) combined
            GROUP BY p_id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresaId", empresaId.toString())
                .addValue("bodegaId", bodegaId.toString());

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String prodIdStr = rs.getString("producto_id");
            UUID prodId = UUID.fromString(prodIdStr);
            int frecuencia = rs.getInt("frecuencia_salida");
            BigDecimal valor = rs.getBigDecimal("valor_total_despachado");
            if (valor == null) {
                valor = BigDecimal.ZERO;
            }
            valor = valor.setScale(4, RoundingMode.HALF_UP);
            return new ProductoMetricaSalidaDto(prodId, frecuencia, valor);
        });
    }
}
