package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdersIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PedidoJpaAdapter pedidoJpaAdapter;

    @Autowired
    private PedidoJpaRepository pedidoJpaRepository;

    private static final UUID EMPRESA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        pedidoJpaRepository.deleteAll();
    }

    @Test
    void test1_crearPedidoYPersistirExitoso() {
        Pedido pedido = Pedido.crear(
                new PedidoId(UUID.randomUUID()),
                EMPRESA_ID,
                new ClienteId(UUID.randomUUID()),
                "testUser"
        );
        pedido.agregarLinea(new ProductoId(UUID.randomUUID()), 5, new Dinero(new BigDecimal("10.50")));

        assertDoesNotThrow(() -> pedidoJpaAdapter.save(pedido));
    }

    @Test
    @Transactional
    void test2_optimisticLockingAlModificarConcurrente() {
        Pedido pedido = Pedido.crear(
                new PedidoId(UUID.randomUUID()),
                EMPRESA_ID,
                new ClienteId(UUID.randomUUID()),
                "testUser"
        );
        pedido.agregarLinea(new ProductoId(UUID.randomUUID()), 1, new Dinero(new BigDecimal("10.00")));
        pedidoJpaAdapter.save(pedido);

        // Simulamos recuperar en hilo 1
        Pedido pedidoHilo1 = pedidoJpaAdapter.findByIdAndEmpresaId(pedido.getId(), EMPRESA_ID).orElseThrow();
        // Simulamos recuperar en hilo 2
        Pedido pedidoHilo2 = pedidoJpaAdapter.findByIdAndEmpresaId(pedido.getId(), EMPRESA_ID).orElseThrow();

        // Hilo 1 actualiza y guarda
        pedidoHilo1.confirmar();
        pedidoJpaAdapter.save(pedidoHilo1);

        // Hilo 2 intenta guardar, debe fallar por Optimistic Locking (versión desactualizada)
        pedidoHilo2.agregarLinea(new ProductoId(UUID.randomUUID()), 1, new Dinero(new BigDecimal("5.00")));
        
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            pedidoJpaAdapter.save(pedidoHilo2);
        });
    }
}
