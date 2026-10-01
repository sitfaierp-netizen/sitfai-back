package com.SITFAI_CORE_ERP_TIENDA.iam_module.application;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.*;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception.IdentityProvisioningException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.IdentityProvisioningPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRegistrationUnitOfWork;
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

    @Mock
    private UsuarioRegistrationUnitOfWork registrationUnitOfWork;

    private RegistrarUsuarioService registrarUsuarioService;
    private GestionarEstadoUsuarioService gestionarEstadoUsuarioService;
    private CambiarRolUsuarioService cambiarRolUsuarioService;
    private ConsultarUsuarioService consultarUsuarioService;

    private final UUID empresaId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registrarUsuarioService = new RegistrarUsuarioService(usuarioRepository, registrationUnitOfWork, identityProvisioningPort);
        gestionarEstadoUsuarioService = new GestionarEstadoUsuarioService(usuarioRepository, usuarioEventPublisher, identityProvisioningPort);
        cambiarRolUsuarioService = new CambiarRolUsuarioService(usuarioRepository, usuarioEventPublisher, identityProvisioningPort);
        consultarUsuarioService = new ConsultarUsuarioService(usuarioRepository);
    }

    private Usuario pendientePersistido(Usuario source) {
        return Usuario.reconstituir(
                source.getId(), source.getEmpresaId(), source.getUsername(), source.getEmail(), source.getRol(),
                com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.EstadoUsuario.PENDIENTE_IDENTIDAD,
                source.getCreadoEn(), source.getActualizadoEn(), true, null, null
        );
    }

    private Usuario usuarioPendiente(UUID id, String username, String email, RolUsuario rol) {
        return Usuario.registrar(
                UsuarioId.de(id), EmpresaId.de(empresaId), Username.de(username), Email.de(email), rol
        );
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

            when(usuarioRepository.buscarPorUsername(any(EmpresaId.class), any(Username.class))).thenReturn(Optional.empty());
            when(usuarioRepository.buscarPorEmail(any(EmpresaId.class), any(Email.class))).thenReturn(Optional.empty());
            when(registrationUnitOfWork.guardarPendiente(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(registrationUnitOfWork.confirmarIdentidad(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UsuarioResponse response = registrarUsuarioService.ejecutar(command);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(usuarioId);
            assertThat(response.empresaId()).isEqualTo(empresaId);
            assertThat(response.username()).isEqualTo("cajero01");
            assertThat(response.email()).isEqualTo("cajero01@empresa.com");
            assertThat(response.rol()).isEqualTo("CAJERO");
            assertThat(response.estado()).isEqualTo("ACTIVO");

            verify(registrationUnitOfWork).guardarPendiente(any(Usuario.class));
            verify(registrationUnitOfWork).confirmarIdentidad(any(Usuario.class));
            verify(identityProvisioningPort).provisionar(any(Usuario.class));
            verify(identityProvisioningPort).completarOnboarding(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe conservar el alta pendiente si Keycloak no está disponible y permitir reintento")
        void debeConservarPendienteCuandoKeycloakNoEstaDisponible() {
            RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero01@empresa.com",
                    "CAJERO"
            );
            when(usuarioRepository.buscarPorUsername(any(), any())).thenReturn(Optional.empty());
            when(usuarioRepository.buscarPorEmail(any(), any())).thenReturn(Optional.empty());
            when(registrationUnitOfWork.guardarPendiente(any())).thenAnswer(invocation -> invocation.getArgument(0));
            doThrow(new IdentityProvisioningException("Keycloak no está disponible."))
                    .doNothing()
                    .when(identityProvisioningPort).provisionar(any(Usuario.class));
            when(registrationUnitOfWork.confirmarIdentidad(any())).thenAnswer(invocation -> invocation.getArgument(0));

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(IdentityProvisioningException.class);

            ArgumentCaptor<Usuario> pendienteCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(registrationUnitOfWork).guardarPendiente(pendienteCaptor.capture());
            Usuario pendiente = pendientePersistido(pendienteCaptor.getValue());
            assertThat(pendiente.estaPendienteIdentidad()).isTrue();
            when(usuarioRepository.buscarPorUsername(any(), any())).thenReturn(Optional.of(pendiente));
            when(usuarioRepository.buscarPorEmail(any(), any())).thenReturn(Optional.of(pendiente));

            UsuarioResponse response = registrarUsuarioService.ejecutar(command);
            assertThat(response.id()).isEqualTo(usuarioId);
            verify(identityProvisioningPort, times(2)).provisionar(any(Usuario.class));
            verify(registrationUnitOfWork).confirmarIdentidad(any(Usuario.class));
            verify(identityProvisioningPort).completarOnboarding(any(Usuario.class));
        }

        @Test
        @DisplayName("EXTERNAL_SUCCESS_LOCAL_FAILURE_RETRY mantiene el UUID servidor con id null")
        void debeRecuperarExitoExternoSeguidoDeFalloLocalConIdNulo() {
            RegistrarUsuarioCommand command = new RegistrarUsuarioCommand(
                    null,
                    empresaId,
                    "cajero01",
                    "cajero01@empresa.com",
                    "CAJERO"
            );
            when(usuarioRepository.buscarPorUsername(any(), any())).thenReturn(Optional.empty());
            when(usuarioRepository.buscarPorEmail(any(), any())).thenReturn(Optional.empty());
            when(registrationUnitOfWork.guardarPendiente(any())).thenAnswer(invocation -> invocation.getArgument(0));
            doThrow(new IllegalStateException("fallo local inyectado"))
                    .doAnswer(invocation -> invocation.getArgument(0))
                    .when(registrationUnitOfWork).confirmarIdentidad(any());

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(IllegalStateException.class);

            ArgumentCaptor<Usuario> pendienteCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(registrationUnitOfWork).guardarPendiente(pendienteCaptor.capture());
            Usuario pendiente = pendientePersistido(pendienteCaptor.getValue());
            UUID idServidor = pendiente.getId().valor();
            when(usuarioRepository.buscarPorUsername(any(), any())).thenReturn(Optional.of(pendiente));
            when(usuarioRepository.buscarPorEmail(any(), any())).thenReturn(Optional.of(pendiente));

            UsuarioResponse response = registrarUsuarioService.ejecutar(command);

            assertThat(response.id()).isEqualTo(idServidor);
            assertThat(response.estado()).isEqualTo("ACTIVO");
            ArgumentCaptor<Usuario> externalCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(identityProvisioningPort, times(2)).provisionar(externalCaptor.capture());
            assertThat(externalCaptor.getAllValues())
                    .extracting(usuario -> usuario.getId().valor())
                    .containsOnly(idServidor);
            verify(registrationUnitOfWork, times(1)).guardarPendiente(any());
            verify(identityProvisioningPort, times(1)).completarOnboarding(any());
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

            Usuario existente = usuarioPendiente(usuarioId, "cajero01", "otro@empresa.com", RolUsuario.CAJERO);
            when(usuarioRepository.buscarPorUsername(any(EmpresaId.class), any(Username.class)))
                    .thenReturn(Optional.of(existente));
            when(usuarioRepository.buscarPorEmail(any(EmpresaId.class), any(Email.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("ya pertenece a otro usuario");

            verify(registrationUnitOfWork, never()).guardarPendiente(any());
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

            Usuario existente = usuarioPendiente(usuarioId, "otro01", "cajero01@empresa.com", RolUsuario.CAJERO);
            when(usuarioRepository.buscarPorUsername(any(EmpresaId.class), any(Username.class))).thenReturn(Optional.empty());
            when(usuarioRepository.buscarPorEmail(any(EmpresaId.class), any(Email.class)))
                    .thenReturn(Optional.of(existente));

            assertThatThrownBy(() -> registrarUsuarioService.ejecutar(command))
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("ya pertenece a otro usuario");

            verify(registrationUnitOfWork, never()).guardarPendiente(any());
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
            usuario.confirmarIdentidad();
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
            usuario.confirmarIdentidad();
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
            usuario.confirmarIdentidad();
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
