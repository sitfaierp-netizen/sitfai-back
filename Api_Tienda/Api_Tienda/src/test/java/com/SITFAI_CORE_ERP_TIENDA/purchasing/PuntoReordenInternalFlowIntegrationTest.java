package com.SITFAI_CORE_ERP_TIENDA.purchasing;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.PuntoReordenAlcanzadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository.OrdenCompraJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class PuntoReordenInternalFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OrdenCompraJpaRepository ordenCompraRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PUNTO_REORDEN_INTERNAL_EVENT_WITHOUT_SECURITY_CONTEXT")
    void puntoReordenInternalEventWithoutSecurityContext() {
        SecurityContextHolder.clearContext();
        UUID empresaId = UUID.randomUUID();
        UUID bodegaId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();

        publicarEvento(empresaId, bodegaId, productoId);

        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            List<OrdenPersistida> ordenes = consultarOrdenes(empresaId);
            assertThat(ordenes).hasSize(1);

            OrdenPersistida orden = ordenes.getFirst();
            assertThat(orden.empresaId()).isEqualTo(empresaId.toString());
            assertThat(orden.lineas()).hasSize(1);
            assertThat(orden.lineas().getFirst().empresaId()).isEqualTo(empresaId.toString());
            assertThat(orden.lineas().getFirst().productoId()).isEqualTo(productoId.toString());
        });
    }

    @Test
    @DisplayName("INTERNAL_EVENT_TENANT_IS_PRESERVED")
    void internalEventTenantIsPreserved() {
        SecurityContextHolder.clearContext();
        UUID empresaA = UUID.randomUUID();
        UUID empresaB = UUID.randomUUID();
        UUID productoA = UUID.randomUUID();
        UUID productoB = UUID.randomUUID();

        publicarEvento(empresaA, UUID.randomUUID(), productoA);
        publicarEvento(empresaB, UUID.randomUUID(), productoB);

        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OrdenPersistida> ordenesA = consultarOrdenes(empresaA);
            List<OrdenPersistida> ordenesB = consultarOrdenes(empresaB);
            assertThat(ordenesA).hasSize(1);
            assertThat(ordenesB).hasSize(1);

            OrdenPersistida ordenA = ordenesA.getFirst();
            OrdenPersistida ordenB = ordenesB.getFirst();
            assertThat(ordenA.lineas()).extracting(LineaPersistida::productoId)
                    .containsExactly(productoA.toString());
            assertThat(ordenB.lineas()).extracting(LineaPersistida::productoId)
                    .containsExactly(productoB.toString());
            assertThat(ordenCompraRepository.findByIdAndEmpresaId(ordenB.id(), empresaA.toString()))
                    .as("Una consulta tenant A no debe devolver la orden tenant B")
                    .isEmpty();
            assertThat(ordenCompraRepository.findByIdAndEmpresaId(ordenA.id(), empresaB.toString()))
                    .as("Una consulta tenant B no debe devolver la orden tenant A")
                    .isEmpty();
        });
    }

    private void publicarEvento(UUID empresaId, UUID bodegaId, UUID productoId) {
        PuntoReordenAlcanzadoEvent evento = PuntoReordenAlcanzadoEvent.of(
                new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId(empresaId),
                new BodegaId(bodegaId),
                new ProductoId(productoId),
                new BigDecimal("5.0000")
        );

        transactionTemplate.executeWithoutResult(status -> applicationEventPublisher.publishEvent(evento));
    }

    private List<OrdenPersistida> consultarOrdenes(UUID empresaId) {
        return transactionTemplate.execute(status -> ordenCompraRepository.findByEmpresaId(empresaId.toString())
                .stream()
                .map(orden -> new OrdenPersistida(
                        orden.getId(),
                        orden.getEmpresaId(),
                        orden.getLineas().stream()
                                .map(linea -> new LineaPersistida(linea.getEmpresaId(), linea.getProductoId()))
                                .toList()
                ))
                .toList());
    }

    private record OrdenPersistida(String id, String empresaId, List<LineaPersistida> lineas) {}

    private record LineaPersistida(String empresaId, String productoId) {}
}
