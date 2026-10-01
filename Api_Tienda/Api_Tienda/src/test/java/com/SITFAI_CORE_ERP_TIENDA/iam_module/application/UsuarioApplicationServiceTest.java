package com.SITFAI_CORE_ERP_TIENDA.iam_module.application;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.*;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception.IdentityProvisioningException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.IdentityProvisioningPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service.CambiarRolUsuarioService;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service.ConsultarUsuarioService;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service.GestionarEstadoUsuarioService;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service.RegistrarUsuarioService;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.RolUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Capa de Aplicación: IAM Module (Use Cases, Services y Mappers)")
class UsuarioApplicationServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioEventPublisher usuarioEventPublisher;

    @Mock
    private IdentityProvisioningPort identityProvisioningPort;

    private RegistrarUsuarioService registrarUsuarioService;
    private GestionarEstadoUsuarioService gestionarEstadoUsuarioService;
    private CambiarRolUsuarioService cambiarRolUsuarioService;
    private ConsultarUsuarioService consultarUsuarioService;

    private final UUID empresaId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registrarUsuarioService = new RegistrarUsuarioService(usuarioRepository, usuarioEventPublisher, identityProvisioningPort);
        gestionarEstadoUsuarioService = new GestionarEstadoUsuarioService(usuarioRepository, usuarioEventPublisher, identityProvisioningPort);
        cambiarRolUsuarioService = new CambiarRolUsuarioService(usuarioRepository, usuarioEventPublisher, identityProvisioningPort);
        consultarUsuarioService = new ConsultarUsuarioService(usuarioRepository);
    }

    @Nested
    @DisplayName("Caso de Uso: Registrar Usuario")
    class RegistrarUsuarioTests {

        @Test
        @DisplayName("Debe registrar un nuevo usuario, persistir y publicar evento exitosamente")
        void debeRegistrarUsuarioExitosamente() {
            RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero01@empresa.com",
                    "CAJERO"
            );

            when(usuarioRepository.existePorUsername(any(EmpresaId.class), any(Username.class))).thenReturn(false);
            when(usuarioRepository.existePorEmail(any(EmpresaId.class), any(Email.class))).thenReturn(false);
            when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UsuarioResponse response = registrarUsuarioService.ejecutar(command);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(usuarioId);
            assertThat(response.empresaId()).isEqualTo(empresaId);
            assertThat(response.username()).isEqualTo("cajero01");
            assertThat(response.email()).isEqualTo("cajero01@empresa.com");
            assertThat(response.rol()).isEqualTo("CAJERO");
            assertThat(response.estado()).isEqualTo("ACTIVO");

            verify(usuarioRepository).guardar(any(Usuario.class));
            verify(usuarioEventPublisher).publicarTodos(anyList());
            verify(identityProvisioningPort).provisionar(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe revertir el alta local si Keycloak no está disponible y permitir reintento")
        void debeFallarSinPersistirCuandoKeycloakNoEstaDisponible() {
            RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero01@empresa.com",
                    "CAJERO"
            );
            when(usuarioRepository.existePorUsername(any(), any())).thenReturn(false);
            when(usuarioRepository.existePorEmail(any(), any())).thenReturn(false);
            doThrow(new IdentityProvisioningException("Keycloak no está disponible."))
                    .doNothing()
                    .when(identityProvisioningPort).provisionar(any(Usuario.class));
            when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(IdentityProvisioningException.class);
            verify(usuarioRepository, never()).guardar(any());

            UsuarioResponse response = registrarUsuarioService.ejecutar(command);
            assertThat(response.id()).isEqualTo(usuarioId);
            verify(identityProvisioningPort, times(2)).provisionar(any(Usuario.class));
            verify(usuarioRepository).guardar(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe rechazar registro si el username ya está registrado en la empresa")
        void debeRechazarUsernameDuplicado() {
            RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero01@empresa.com",
                    "CAJERO"
            );

            when(usuarioRepository.existePorUsername(any(EmpresaId.class), any(Username.class))).thenReturn(true);

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("ya está registrado en esta empresa");

            verify(usuarioRepository, never()).guardar(any());
            verify(usuarioEventPublisher, never()).publicarTodos(any());
        }

        @Test
        @DisplayName("Debe rechazar registro si el email ya está registrado en la empresa")
        void debeRechazarEmailDuplicado() {
            RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero01@empresa.com",
                    "CAJERO"
            );

            when(usuarioRepository.existePorUsername(any(EmpresaId.class), any(Username.class))).thenReturn(false);
            when(usuarioRepository.existePorEmail(any(EmpresaId.class), any(Email.class))).thenReturn(true);

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("ya está registrado en esta empresa");

            verify(usuarioRepository, never()).guardar(any());
        }

        @Test
        @DisplayName("Debe rechazar rol inválido en el comando")
        void debeRechazarRolInvalido() {
            RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero01@empresa.com",
                    "ROL_INVENTADO"
            );

            when(usuarioRepository.existePorUsername(any(EmpresaId.class), any(Username.class))).thenReturn(false);
            when(usuarioRepository.existePorEmail(any(EmpresaId.class), any(Email.class))).thenReturn(false);

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("no es un rol válido");
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Gestionar Estado (Desactivar / Reactivar)")
    class GestionarEstadoTests {

        @Test
        @DisplayName("Debe desactivar usuario y publicar eventos")
        void debeDesactivarUsuario() {
            Usuario usuario = Usuario.registrar(
                    UsuarioId.de(usuarioId),
                    EmpresaId.de(empresaId),
                    Username.de("usuario01"),
                    Email.de("usuario01@empresa.com"),
                    RolUsuario.CAJERO
            );
            usuario.pullDomainEvents();

            when(usuarioRepository.buscarPorId(EmpresaId.de(empresaId), UsuarioId.de(usuarioId)))
                    .thenReturn(Optional.of(usuario));
            when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            DesactivarUsuarioCommand command = new DesactivarUsuarioCommand(usuarioId, empresaId, "Despido");
            UsuarioResponse response = gestionarEstadoUsuarioService.ejecutar(command);

            assertThat(response.estado()).isEqualTo("INACTIVO");
            verify(usuarioRepository).guardar(usuario);
            verify(usuarioEventPublisher).publicarTodos(anyList());
            verify(identityProvisioningPort).sincronizarEstado(usuario);
        }

        @Test
        @DisplayName("Debe reactivar usuario inactivo y publicar eventos")
        void debeReactivarUsuario() {
            Usuario usuario = Usuario.registrar(
                    UsuarioId.de(usuarioId),
                    EmpresaId.de(empresaId),
                    Username.de("usuario01"),
                    Email.de("usuario01@empresa.com"),
                    RolUsuario.CAJERO
            );
            usuario.desactivar();
            usuario.pullDomainEvents();

            when(usuarioRepository.buscarPorId(EmpresaId.de(empresaId), UsuarioId.de(usuarioId)))
                    .thenReturn(Optional.of(usuario));
            when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            ReactivarUsuarioCommand command = new ReactivarUsuarioCommand(usuarioId, empresaId);
            UsuarioResponse response = gestionarEstadoUsuarioService.ejecutar(command);

            assertThat(response.estado()).isEqualTo("ACTIVO");
            verify(usuarioRepository).guardar(usuario);
            verify(usuarioEventPublisher).publicarTodos(anyList());
            verify(identityProvisioningPort).sincronizarEstado(usuario);
        }

        @Test
        @DisplayName("Debe lanzar UsuarioNoEncontradoException si el usuario no existe")
        void debeLanzarExcepcionSiUsuarioNoExiste() {
            when(usuarioRepository.buscarPorId(any(), any())).thenReturn(Optional.empty());

            DesactivarUsuarioCommand command = new DesactivarUsuarioCommand(usuarioId, empresaId, "Motivo");
            assertThatThrownBy(() -> gestionarEstadoUsuarioService.ejecutar(command))
                    .isInstanceOf(UsuarioNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Cambiar Rol")
    class CambiarRolTests {

        @Test
        @DisplayName("Debe cambiar rol exitosamente y publicar evento")
        void debeCambiarRol() {
            Usuario usuario = Usuario.registrar(
                    UsuarioId.de(usuarioId),
                    EmpresaId.de(empresaId),
                    Username.de("usuario01"),
                    Email.de("usuario01@empresa.com"),
                    RolUsuario.CAJERO
            );
            usuario.pullDomainEvents();

            when(usuarioRepository.buscarPorId(EmpresaId.de(empresaId), UsuarioId.de(usuarioId)))
                    .thenReturn(Optional.of(usuario));
            when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            CambiarRolUsuarioCommand command = new CambiarRolUsuarioCommand(usuarioId, empresaId, "SUCURSAL_MANAGER");
            UsuarioResponse response = cambiarRolUsuarioService.ejecutar(command);

            assertThat(response.rol()).isEqualTo("SUCURSAL_MANAGER");
            verify(usuarioRepository).guardar(usuario);
            verify(usuarioEventPublisher).publicarTodos(anyList());
            verify(identityProvisioningPort).sincronizarRol(usuario);
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Consultar Usuario")
    class ConsultarUsuarioTests {

        @Test
        @DisplayName("Debe consultar usuario por ID")
        void debeObtenerPorId() {
            Usuario usuario = Usuario.registrar(
                    UsuarioId.de(usuarioId),
                    EmpresaId.de(empresaId),
                    Username.de("usuario01"),
                    Email.de("usuario01@empresa.com"),
                    RolUsuario.CAJERO
            );

            when(usuarioRepository.buscarPorId(EmpresaId.de(empresaId), UsuarioId.de(usuarioId)))
                    .thenReturn(Optional.of(usuario));

            UsuarioResponse response = consultarUsuarioService.obtenerPorId(empresaId, usuarioId);

            assertThat(response).isNotNull();
            assertThat(response.username()).isEqualTo("usuario01");
        }

        @Test
        @DisplayName("Debe listar usuarios por empresa")
        void debeListarPorEmpresa() {
            Usuario usuario = Usuario.registrar(
                    UsuarioId.de(usuarioId),
                    EmpresaId.de(empresaId),
                    Username.de("usuario01"),
                    Email.de("usuario01@empresa.com"),
                    RolUsuario.CAJERO
            );

            when(usuarioRepository.buscarPorEmpresa(EmpresaId.de(empresaId)))
                    .thenReturn(List.of(usuario));

            List<UsuarioResponse> list = consultarUsuarioService.listarPorEmpresa(empresaId);

            assertThat(list).hasSize(1);
            assertThat(list.get(0).username()).isEqualTo("usuario01");
        }
    }
}
