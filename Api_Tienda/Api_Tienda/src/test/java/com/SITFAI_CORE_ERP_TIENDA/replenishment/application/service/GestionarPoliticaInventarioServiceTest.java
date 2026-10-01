package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.GestionarPoliticaInventarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.NivelOptimo;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PuntoReorden;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output.PoliticaInventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionarPoliticaInventarioServiceTest {

    @Mock
    private PoliticaInventarioRepository repository;

    private GestionarPoliticaInventarioService service;

    @BeforeEach
    void setUp() {
        service = new GestionarPoliticaInventarioService(repository);
        when(repository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void crearOrquestaElDominioSinExponerElRepositorioAlAdaptadorWeb() {
        UUID empresaId = UUID.randomUUID();
        GestionarPoliticaInventarioCommand command = command(null, empresaId);

        var result = service.crear(command);

        assertNotNull(result.id());
        assertEquals(empresaId, result.empresaId());
        assertEquals(10, result.puntoReorden());
        assertEquals(50, result.nivelOptimo());
        verify(repository).guardar(any(PoliticaInventario.class));
    }

    @Test
    void actualizarConsultaPorIdYTenantAntesDeGuardar() {
        UUID politicaId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        GestionarPoliticaInventarioCommand command = command(politicaId, empresaId);
        PoliticaInventario existente = new PoliticaInventario(
                new PoliticaId(politicaId),
                new EmpresaId(empresaId),
                new BodegaId(command.bodegaId()),
                new ProductoId(command.productoId()),
                new PuntoReorden(5),
                new NivelOptimo(20),
                true
        );
        when(repository.buscarPorId(new PoliticaId(politicaId), new EmpresaId(empresaId)))
                .thenReturn(Optional.of(existente));

        var result = service.actualizar(command);

        assertEquals(politicaId, result.id());
        assertEquals(empresaId, result.empresaId());
        verify(repository).buscarPorId(new PoliticaId(politicaId), new EmpresaId(empresaId));
        verify(repository).guardar(any(PoliticaInventario.class));
    }

    private GestionarPoliticaInventarioCommand command(UUID politicaId, UUID empresaId) {
        return new GestionarPoliticaInventarioCommand(
                politicaId,
                empresaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                50,
                true
        );
    }
}
