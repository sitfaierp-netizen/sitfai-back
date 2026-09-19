package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.LineaRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.Recepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.RecepcionJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ApiTiendaApplication.class)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RecepcionIntegrationTest {

    @Autowired
    private RecepcionJpaAdapter recepcionJpaAdapter;

    @Autowired
    private RecepcionJpaRepository recepcionJpaRepository;

    private EmpresaId empresaId;
    private RecepcionId recepcionId;

    @BeforeEach
    void setUp() {
        recepcionJpaRepository.deleteAll();
        empresaId = new EmpresaId(UUID.randomUUID());
        recepcionId = RecepcionId.generar();
    }

    @AfterEach
    void tearDown() {
        recepcionJpaRepository.deleteAll();
    }

    @Test
    void guardar_y_buscarPorId_guardaRecuperaConLineasYLotes() {
        // Arrange
        BodegaId bodegaDestinoId = new BodegaId(UUID.randomUUID());
        OrdenCompraId ordenCompraOrigenId = new OrdenCompraId(UUID.randomUUID());

        Recepcion recepcion = Recepcion.crearBorrador(
                recepcionId,
                empresaId,
                bodegaDestinoId,
                ordenCompraOrigenId,
                "TEST_USER"
        );

        Lote lote = new Lote("LOTE-123", LocalDate.now().plusDays(30));
        LineaRecepcion linea = new LineaRecepcion(
                new ProductoId(UUID.randomUUID()),
                new Cantidad(BigDecimal.valueOf(100)),
                lote
        );
        recepcion.agregarLinea(linea);
        
        // Act
        recepcionJpaAdapter.guardar(recepcion);

        // Assert
        Optional<Recepcion> encontradaOpt = recepcionJpaAdapter.buscarPorIdYEmpresaId(recepcionId, empresaId);
        assertTrue(encontradaOpt.isPresent());

        Recepcion encontrada = encontradaOpt.get();
        assertEquals(recepcionId.valor(), encontrada.getId().valor());
        assertEquals(empresaId.valor(), encontrada.getEmpresaId().valor());
        assertEquals(bodegaDestinoId.valor(), encontrada.getBodegaDestino().valor());
        
        assertEquals(1, encontrada.getLineas().size());
        LineaRecepcion lineaEncontrada = encontrada.getLineas().get(0);
        assertEquals(BigDecimal.valueOf(100).setScale(4), lineaEncontrada.getCantidadRecibida().valor().setScale(4));
        assertEquals("LOTE-123", lineaEncontrada.getLote().codigoLote());
        assertNotNull(lineaEncontrada.getLote().fechaCaducidad());
    }
}
