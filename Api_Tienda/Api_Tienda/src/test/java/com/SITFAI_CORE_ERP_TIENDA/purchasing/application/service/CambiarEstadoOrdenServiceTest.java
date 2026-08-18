package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.OrdenCompraRecibidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProveedorId;
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

    @InjectMocks
    private CambiarEstadoOrdenService service;

    private UUID empresaId;
    private UUID ordenId;
    private OrdenCompra ordenSimulada;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        ordenId = UUID.randomUUID();
        ordenSimulada = OrdenCompra.crearBorrador(new OrdenCompraId(ordenId), new EmpresaId(empresaId), new ProveedorId(UUID.randomUUID()));
        ordenSimulada.agregarLinea(new LineaOrdenCompra(new ProductoId(UUID.randomUUID()), BigDecimal.ONE, Dinero.de(10)));
        ordenSimulada.emitir(); // Para que pueda ser RECIBIDA
    }

    @Test
    void dadoOrdenEmitida_cuandoTransicionaARecibida_entoncesGuardaYPublicaEvento() {
        CambiarEstadoCommand command = new CambiarEstadoCommand(ordenId, empresaId, "RECIBIDA");

        when(repository.buscarPorId(any(OrdenCompraId.class), any(EmpresaId.class))).thenReturn(Optional.of(ordenSimulada));

        OrdenCompraResponse response = service.cambiarEstado(command);

        assertNotNull(response);
        assertEquals("RECIBIDA", response.estado());
        verify(repository).guardar(any(OrdenCompra.class));
        verify(eventPublisher).publicar(any(OrdenCompraRecibidaEvent.class)); // Se emite al marcar como recibida
    }
}
