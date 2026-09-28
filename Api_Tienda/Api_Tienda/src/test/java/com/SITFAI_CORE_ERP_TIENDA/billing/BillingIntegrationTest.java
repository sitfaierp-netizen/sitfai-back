package com.SITFAI_CORE_ERP_TIENDA.billing;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.port.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EstadoFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.messaging.PedidoConfirmadoEventHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de IntegraciÃ³n: Verifica el flujo asÃ­ncrono desde el E-Commerce hacia FacturaciÃ³n.
 * <p>
 * Simula la publicaciÃ³n de {@link PedidoConfirmadoEvent} y comprueba que la factura
 * se genera en estado {@link EstadoFactura#EMITIDA}, con sus totales calculados,
 * vinculada al documento origen y almacenada con aislamiento MT-01.
 */
@SpringBootTest(classes = ApiTiendaApplication.class)
@Testcontainers
@ActiveProfiles("test")
public class BillingIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("sitfai_tienda")
            .withUsername("sitfai_user")
            .withPassword("sitfai_secret_pwd");

    @Autowired
    private PedidoConfirmadoEventHandler pedidoConfirmadoEventHandler;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    @DisplayName("Debe emitir y persistir la factura correctamente al recibir PedidoConfirmadoEvent")
    void alRecibirPedidoConfirmadoEvent_debeEmitirYPersistirFacturaEnBaseDeDatos() {
        // 1. ARRANGE
        UUID empresaIdRaw = UUID.randomUUID();
        UUID clienteIdRaw = UUID.randomUUID();
        UUID pedidoIdRaw = UUID.randomUUID();
        UUID productoIdRaw = UUID.randomUUID();

        EmpresaId empresaId = EmpresaId.de(empresaIdRaw);
        ClienteId clienteId = ClienteId.de(clienteIdRaw);
        PedidoId pedidoId = PedidoId.de(pedidoIdRaw);
        ProductoId productoId = ProductoId.de(productoIdRaw);

        LineaPedido linea = LineaPedido.crear(
                productoId,
                2,
                Dinero.de(new BigDecimal("50000.00"), "COP")
        );

        PedidoConfirmadoEvent event = PedidoConfirmadoEvent.of(
                pedidoId,
                empresaId,
                clienteId,
                Dinero.de(new BigDecimal("100000.00"), "COP"),
                List.of(linea)
        );

        // 2. ACT: Simular la recepciÃ³n del evento de confirmaciÃ³n de pedido
        pedidoConfirmadoEventHandler.onPedidoConfirmado(event);

        // 3. ASSERT: Verificar persistencia con aislamiento MT-01
        com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId tenantId =
                com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId.de(empresaIdRaw);

        List<Factura> facturas = facturaRepository.buscarPorEmpresa(tenantId);
        assertThat(facturas).isNotEmpty();

        Factura factura = facturas.stream()
                .filter(f -> f.getDocumentoFuenteId().numero().equals(pedidoIdRaw.toString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontrÃ³ la factura para el pedido " + pedidoIdRaw));

        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.EMITIDA);
        assertThat(factura.getEmpresaId().valor()).isEqualTo(empresaIdRaw);
        assertThat(factura.getClienteId().valor()).isEqualTo(clienteIdRaw);
        assertThat(factura.getDocumentoFuenteId().tipo()).contains("ECOMMERCE");
        assertThat(factura.getLineas()).hasSize(1);

        // Validar cÃ¡lculo de totales matemÃ¡ticos (Subtotal 100,000 + IVA 19% = 119,000)
        assertThat(factura.getSubtotal().monto()).isEqualByComparingTo(new BigDecimal("100000.0000"));
        assertThat(factura.getTotalImpuestos().monto()).isEqualByComparingTo(new BigDecimal("19000.0000"));
        assertThat(factura.getTotal().monto()).isEqualByComparingTo(new BigDecimal("119000.0000"));
    }
}

