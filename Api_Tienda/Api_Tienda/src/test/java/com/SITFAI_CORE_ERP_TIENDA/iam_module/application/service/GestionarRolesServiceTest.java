package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CrearRolCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.PermisoModuloDto;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RolResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.ModuloSistema;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Rol;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out.RolEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out.RolRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GestionarRolesServiceTest {

    @Mock
    private RolRepositoryPort rolRepositoryPort;

    @Mock
    private RolEventPublisherPort rolEventPublisherPort;

    @InjectMocks
    private GestionarRolesService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crearRol_Success() {
        // Arrange
        CrearRolCommand command = new CrearRolCommand();
        command.setCodigo("ADMIN");
        command.setNombre("Administrador");
        command.setDescripcion("Rol admin");
        command.setPermisos(List.of(new PermisoModuloDto(ModuloSistema.IAM, Set.of("READ", "WRITE"))));

        when(rolRepositoryPort.buscarPorCodigo("ADMIN")).thenReturn(Optional.empty());
        when(rolRepositoryPort.guardar(any(Rol.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        RolResponse response = service.crearRol(command);

        // Assert
        assertNotNull(response);
        assertEquals("ADMIN", response.getCodigo());
        assertEquals(1, response.getPermisos().size());
        verify(rolEventPublisherPort, times(1)).publicar(any());
        verify(rolRepositoryPort, times(1)).guardar(any(Rol.class));
    }

    @Test
    void crearRol_ThrowsExceptionIfCodigoExists() {
        // Arrange
        CrearRolCommand command = new CrearRolCommand();
        command.setCodigo("ADMIN");

        when(rolRepositoryPort.buscarPorCodigo("ADMIN")).thenReturn(Optional.of(new Rol("1", "ADMIN", "Adm", "Desc", List.of())));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.crearRol(command));
        verify(rolRepositoryPort, never()).guardar(any());
    }
}
