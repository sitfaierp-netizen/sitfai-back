package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.ClasificacionProducto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.port.ClasificacionProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.CategoriaABC;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de Integración: Análisis ABC de Inventario (WMS Intelligence).
 * <p>
 * Simula la carga de histórico de movimientos de salida en MySQL,
 * dispara el análisis vía REST (POST /inventory/bodegas/{id}/analisis-abc)
 * y certifica que:
 * 1. El endpoint REST responde HTTP 200 con el resumen consolidado de Pareto.
 * 2. Los productos se clasifican en categorías A, B y C de acuerdo a la Ley de Pareto.
 * 3. Las métricas de movimiento y la categoría calculada se persisten correctamente en MySQL.
 */
@SpringBootTest(classes = ApiTiendaApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
public class AnalisisAbcIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ClasificacionProductoRepository clasificacionRepository;

    @MockitoBean
    private TenantProviderPort tenantProviderPort;

    private UUID empresaId;
    private UUID bodegaId;
    private UUID productoAId;
    private UUID productoBId;
    private UUID productoCId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        bodegaId = UUID.randomUUID();
        productoAId = UUID.randomUUID();
        productoBId = UUID.randomUUID();
        productoCId = UUID.randomUUID();

        when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(EmpresaId.de(empresaId));

        // 1. Crear bodega en base de datos para satisfacer FKs de movimientos
        String insertBodegaSql = """
            INSERT INTO inventory_bodega (id, empresa_id, sucursal_id, codigo, nombre, activa, creado_en, actualizado_en)
            VALUES (:id, :empresaId, :sucursalId, :codigo, :nombre, 1, NOW(), NOW())
        """;
        jdbcTemplate.update(insertBodegaSql, new MapSqlParameterSource()
                .addValue("id", bodegaId.toString())
                .addValue("empresaId", empresaId.toString())
                .addValue("sucursalId", UUID.randomUUID().toString())
                .addValue("codigo", "BDG-ABC-" + UUID.randomUUID().toString().substring(0, 5))
                .addValue("nombre", "Bodega Principal Análisis ABC")
        );

        // 2. Simular histórico de movimientos de salida:
        // Producto A: 4 salidas de 200 = 800 (80% del total de 1000) -> Categoría A
        insertarMovimientoSalida(productoAId, new BigDecimal("200.0000"));
        insertarMovimientoSalida(productoAId, new BigDecimal("200.0000"));
        insertarMovimientoSalida(productoAId, new BigDecimal("200.0000"));
        insertarMovimientoSalida(productoAId, new BigDecimal("200.0000"));

        // Producto B: 1 salida de 150 = 150 (15% del total de 1000) -> Categoría B
        insertarMovimientoSalida(productoBId, new BigDecimal("150.0000"));

        // Producto C: 1 salida de 50 = 50 (5% del total de 1000) -> Categoría C
        insertarMovimientoSalida(productoCId, new BigDecimal("50.0000"));
    }

    private void insertarMovimientoSalida(UUID productoId, BigDecimal cantidad) {
        String insertMovSql = """
            INSERT INTO inventory_movimiento (
                id, bodega_id, producto_id, empresa_id, cantidad, tipo, doc_fuente_tipo, doc_fuente_numero, fecha_registro
            ) VALUES (
                :id, :bodegaId, :productoId, :empresaId, :cantidad, 'SALIDA', 'VENTA', :docNum, NOW()
            )
        """;
        jdbcTemplate.update(insertMovSql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID().toString())
                .addValue("bodegaId", bodegaId.toString())
                .addValue("productoId", productoId.toString())
                .addValue("empresaId", empresaId.toString())
                .addValue("cantidad", cantidad)
                .addValue("docNum", "DOC-" + UUID.randomUUID().toString().substring(0, 8))
        );
    }

    @Test
    @DisplayName("Debe ejecutar el análisis ABC vía REST, calcular Pareto correctamente y persistir resultados")
    void debeEjecutarAnalisisAbcYPersistirEnMysql() throws Exception {
        // Ejecutar trigger REST del Análisis ABC
        mockMvc.perform(post("/inventory/bodegas/{id}/analisis-abc", bodegaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bodegaId").value(bodegaId.toString()))
                .andExpect(jsonPath("$.empresaId").value(empresaId.toString()))
                .andExpect(jsonPath("$.totalProductosClasificados").value(3))
                .andExpect(jsonPath("$.totalCategoriaA").value(1))
                .andExpect(jsonPath("$.totalCategoriaB").value(1))
                .andExpect(jsonPath("$.totalCategoriaC").value(1));

        com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId domainEmpresaId =
                new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId(empresaId);
        BodegaId domainBodegaId = new BodegaId(bodegaId);

        // Verificar persistencia de Producto A (Top 80%)
        Optional<ClasificacionProducto> clasifA = clasificacionRepository.buscarPorProducto(
                domainEmpresaId, domainBodegaId, new ProductoId(productoAId));
        assertThat(clasifA).isPresent();
        assertThat(clasifA.get().getCategoria()).isEqualTo(CategoriaABC.A);
        assertThat(clasifA.get().getMetrica().frecuenciaSalida()).isEqualTo(4);
        assertThat(clasifA.get().getMetrica().valorTotalDespachado())
                .isEqualByComparingTo(new BigDecimal("800.0000").setScale(4, RoundingMode.HALF_UP));

        // Verificar persistencia de Producto B (Siguiente 15%)
        Optional<ClasificacionProducto> clasifB = clasificacionRepository.buscarPorProducto(
                domainEmpresaId, domainBodegaId, new ProductoId(productoBId));
        assertThat(clasifB).isPresent();
        assertThat(clasifB.get().getCategoria()).isEqualTo(CategoriaABC.B);
        assertThat(clasifB.get().getMetrica().frecuenciaSalida()).isEqualTo(1);
        assertThat(clasifB.get().getMetrica().valorTotalDespachado())
                .isEqualByComparingTo(new BigDecimal("150.0000").setScale(4, RoundingMode.HALF_UP));

        // Verificar persistencia de Producto C (Último 5%)
        Optional<ClasificacionProducto> clasifC = clasificacionRepository.buscarPorProducto(
                domainEmpresaId, domainBodegaId, new ProductoId(productoCId));
        assertThat(clasifC).isPresent();
        assertThat(clasifC.get().getCategoria()).isEqualTo(CategoriaABC.C);
        assertThat(clasifC.get().getMetrica().frecuenciaSalida()).isEqualTo(1);
        assertThat(clasifC.get().getMetrica().valorTotalDespachado())
                .isEqualByComparingTo(new BigDecimal("50.0000").setScale(4, RoundingMode.HALF_UP));
    }
}
