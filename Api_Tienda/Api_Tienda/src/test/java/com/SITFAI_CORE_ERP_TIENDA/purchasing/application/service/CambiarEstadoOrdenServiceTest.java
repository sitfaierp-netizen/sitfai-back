package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.OrdenCompraEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CambiarEstadoOrdenServiceTest {

    @Mock
    private OrdenCompraRepository repository;

    @Mock
    private OrdenCompraEventPublisher eventPublisher;

    @Mock
    private ActorProviderPort actorProviderPort;

    @Mock
    private CurrentTenantProvider currentTenantProvider;

    @InjectMocks
    private CambiarEstadoOrdenService service;

    private UUID empresaId;
    private UUID ordenId;
    private OrdenCompra ordenSimulada;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        ordenId = UUID.randomUUID();
        ordenSimulada = OrdenCompra.crear(new OrdenCompraId(ordenId), empresaId, new ProveedorId(UUID.randomUUID()), "user1");
        ordenSimulada.pullDomainEvents();
        ordenSimulada.agregarLinea(new LineaOrdenCompra(UUID.randomUUID(), new ProductoId(UUID.randomUUID()), 1, new Dinero(BigDecimal.TEN)));
    }

    @Test
    void dadoOrdenBorrador_cuandoTransicionaAEmitida_entoncesGuardaYPublicaEvento() {
        CambiarEstadoCommand command = new CambiarEstadoCommand(ordenId, empresaId, "EMITIDA");

        when(repository.buscarPorIdYEmpresaId(any(OrdenCompraId.class), any(UUID.class))).thenReturn(Optional.of(ordenSimulada));
        when(actorProviderPort.getCurrentActorId()).thenReturn("user2");
        when(currentTenantProvider.authorizeTenant(empresaId)).thenReturn(empresaId);

        OrdenCompraResponse response = service.cambiarEstado(command);

        assertNotNull(response);
        assertEquals("EMITIDO", response.estado());
        verify(repository).guardar(any(OrdenCompra.class));
        verify(eventPublisher).publicar(any(OrdenCompraEmitidaEvent.class));
    }
}
