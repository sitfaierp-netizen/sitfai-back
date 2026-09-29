package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.event;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservaStockIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private EmpresaId empresaId;
    private ProductoId productoId;
    private Bodega bodegaInicial;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        productoId = new ProductoId(UUID.randomUUID());
        SucursalId sucursalId = new SucursalId(UUID.randomUUID());

        Bodega bodega = Bodega.crear(
                new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId(empresaId.valor()),
                sucursalId,
                "BOD-ECOM",
                "Bodega E-commerce",
                TipoBodega.VENTA
        );

        bodega.registrarIngreso(
                productoId,
                Cantidad.de(new BigDecimal("50.0000")),
                LoteId.de("LOTE-ECOMMERCE"),
                Instant.now().plusSeconds(3600),
                new DocumentoFuenteId("INICIAL", "1")
        );
        bodegaInicial = bodegaRepository.guardar(bodega);
    }

    @Test
    void cuandoSeEmitePedidoCreadoEvent_entoncesSeReservaStock() {
        // Arrange
        PedidoId pedidoId = new PedidoId(UUID.randomUUID());
        ClienteId clienteId = new ClienteId(UUID.randomUUID());

        LineaPedido linea = new LineaPedido(
                UUID.randomUUID(),
                new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId(productoId.valor()),
                10,
                com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero.de(new BigDecimal("150.0000"))
        );

        PedidoCreadoEvent event = PedidoCreadoEvent.of(
                pedidoId,
                empresaId,
                clienteId,
                new Dinero(new BigDecimal("150.0000"), "COP"),
                List.of(linea)
        );

        // Act
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(event);
            return null;
        });

        // Assert
        Bodega bodegaFinal = bodegaRepository.buscarPorId(
                bodegaInicial.getId(),
                new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId(empresaId.valor())
        ).orElseThrow();

        // 50 (inicial) - 10 (reservado) = 40 (disponible)
        BigDecimal stockDisponible = bodegaFinal.consultarStock(productoId);
        BigDecimal stockReservado = bodegaFinal.consultarStockReservado(productoId);

        assertEquals(0, new BigDecimal("40.0000").compareTo(stockDisponible), "El stock disponible deberÃ­a haber disminuido a 40");
        assertEquals(0, new BigDecimal("10.0000").compareTo(stockReservado), "El stock reservado deberÃ­a ser 10");
    }
}

