package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.RolUsuarioModificadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.UsuarioDesactivadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.UsuarioReactivadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.UsuarioRegistradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.EstadoUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.RolUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dominio IAM — Agregado Usuario y Value Objects")
class UsuarioTest {

    private final UsuarioId usuarioId = UsuarioId.generar();
    private final EmpresaId empresaId = EmpresaId.generar();
    private final Username usernameValido = Username.de("cajero.admin_01");
    private final Email emailValido = Email.de("cajero@sitfai-erp.com");

    @Nested
    @DisplayName("Invariantes del Value Object Username")
    class UsernameTests {

        @ParameterizedTest
        @ValueSource(strings = {"juan123", "admin_01", "cajero.norte", "operador-bodega", "user"})
        @DisplayName("Debe aceptar usernames alfanuméricos válidos de mínimo 4 caracteres")
        void debeAceptarUsernameValido(String valor) {
            Username u = Username.de(valor);
            assertThat(u.valor()).isEqualTo(valor);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "a", "   ", ""})
        @DisplayName("Debe rechazar usernames con menos de 4 caracteres o vacíos")
        void debeRechazarUsernameCorto(String valor) {
            assertThatThrownBy(() -> Username.de(valor))
                    .isInstanceOf(UsuarioInvalidoException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"user name", "user@domain", "admin#1", "cajero!"})
        @DisplayName("Debe rechazar usernames con caracteres especiales no permitidos o espacios")
        void debeRechazarCaracteresInvalidos(String valor) {
            assertThatThrownBy(() -> Username.de(valor))
                    .isInstanceOf(UsuarioInvalidoException.class);
        }

        @Test
        @DisplayName("Debe rechazar username nulo")
        void debeRechazarUsernameNulo() {
            assertThatThrownBy(() -> Username.de(null))
                    .isInstanceOf(UsuarioInvalidoException.class);
        }
    }

    @Nested
    @DisplayName("Invariantes del Value Object Email")
    class EmailTests {

        @ParameterizedTest
        @ValueSource(strings = {"admin@sitfai.com", "cajero.norte@empresa.com.pe", "soporte+1@erp.org"})
        @DisplayName("Debe aceptar emails con formato RFC válido y normalizarlos a minúsculas")
        void debeAceptarEmailValido(String valor) {
            Email email = Email.de(valor);
            assertThat(email.valor()).isEqualTo(valor.toLowerCase());
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalido", "sin-arroba.com", "@dominio.com", "usuario@", "usuario@dominio"})
        @DisplayName("Debe rechazar emails con formato inválido")
        void debeRechazarEmailInvalido(String valor) {
            assertThatThrownBy(() -> Email.de(valor))
                    .isInstanceOf(UsuarioInvalidoException.class);
        }

        @Test
        @DisplayName("Debe rechazar email nulo")
        void debeRechazarEmailNulo() {
            assertThatThrownBy(() -> Email.de(null))
                    .isInstanceOf(UsuarioInvalidoException.class);
        }
    }

    @Nested
    @DisplayName("Invariantes de Value Objects UsuarioId y EmpresaId")
    class IdValueObjectsTests {

        @Test
        @DisplayName("Debe generar y convertir UsuarioId y EmpresaId correctamente")
        void debeGestionarIds() {
            UUID randomUuid = UUID.randomUUID();
            UsuarioId uId = UsuarioId.de(randomUuid.toString());
            EmpresaId eId = EmpresaId.de(randomUuid.toString());

            assertThat(uId.valor()).isEqualTo(randomUuid);
            assertThat(eId.valor()).isEqualTo(randomUuid);
        }

        @Test
        @DisplayName("Debe rechazar UUIDs nulos o con formato malformado")
        void debeRechazarUuidsInvalidos() {
            assertThatThrownBy(() -> UsuarioId.de("no-es-un-uuid"))
                    .isInstanceOf(UsuarioInvalidoException.class);
            assertThatThrownBy(() -> EmpresaId.de((UUID) null))
                    .isInstanceOf(UsuarioInvalidoException.class);
        }
    }

    @Nested
    @DisplayName("Registro y Creación de Usuario")
    class RegistroUsuarioTests {

        @Test
        @DisplayName("Debe registrar pendiente y emitir UsuarioRegistradoEvent sólo al confirmar identidad")
        void debeRegistrarUsuarioExitosamente() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.CAJERO
            );

            assertThat(usuario.getId()).isEqualTo(usuarioId);
            assertThat(usuario.getEmpresaId()).isEqualTo(empresaId);
            assertThat(usuario.getUsername()).isEqualTo(usernameValido);
            assertThat(usuario.getEmail()).isEqualTo(emailValido);
            assertThat(usuario.getRol()).isEqualTo(RolUsuario.CAJERO);
            assertThat(usuario.getEstado()).isEqualTo(EstadoUsuario.PENDIENTE_IDENTIDAD);
            assertThat(usuario.estaActivo()).isFalse();
            assertThat(usuario.estaPendienteIdentidad()).isTrue();
            assertThat(usuario.getCreadoEn()).isNotNull();
            assertThat(usuario.getDomainEvents()).isEmpty();

            usuario.confirmarIdentidad();

            List<DomainEvent> events = usuario.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(UsuarioRegistradoEvent.class);

            UsuarioRegistradoEvent event = (UsuarioRegistradoEvent) events.get(0);
            assertThat(event.usuarioId()).isEqualTo(usuarioId);
            assertThat(event.empresaId()).isEqualTo(empresaId);
            assertThat(event.username()).isEqualTo("cajero.admin_01");
            assertThat(event.email()).isEqualTo("cajero@sitfai-erp.com");
            assertThat(event.rol()).isEqualTo("CAJERO");
        }

        @Test
        @DisplayName("Debe rechazar campos nulos durante el registro")
        void debeRechazarCamposNulos() {
            assertThatThrownBy(() -> Usuario.registrar(null, empresaId, usernameValido, emailValido, RolUsuario.CAJERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Usuario.registrar(usuarioId, null, usernameValido, emailValido, RolUsuario.CAJERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Usuario.registrar(usuarioId, empresaId, null, emailValido, RolUsuario.CAJERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Usuario.registrar(usuarioId, empresaId, usernameValido, null, RolUsuario.CAJERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Usuario.registrar(usuarioId, empresaId, usernameValido, emailValido, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Transiciones de Estado de Usuario")
    class EstadoTransicionesTests {

        @Test
        @DisplayName("Debe desactivar un usuario activo y emitir UsuarioDesactivadoEvent")
        void debeDesactivarUsuario() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.BODEGA_OPERATOR
            );
            usuario.confirmarIdentidad();
            usuario.pullDomainEvents();

            usuario.desactivar("Fin de contrato laboral");

            assertThat(usuario.getEstado()).isEqualTo(EstadoUsuario.INACTIVO);
            assertThat(usuario.estaActivo()).isFalse();

            List<DomainEvent> events = usuario.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(UsuarioDesactivadoEvent.class);

            UsuarioDesactivadoEvent event = (UsuarioDesactivadoEvent) events.get(0);
            assertThat(event.usuarioId()).isEqualTo(usuarioId);
            assertThat(event.motivo()).isEqualTo("Fin de contrato laboral");
        }

        @Test
        @DisplayName("Debe rechazar desactivar un usuario que ya está inactivo")
        void debeRechazarDesactivarInactivo() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.SUCURSAL_MANAGER
            );
            usuario.confirmarIdentidad();
            usuario.desactivar();

            assertThatThrownBy(usuario::desactivar)
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("ya se encuentra inactivo");
        }

        @Test
        @DisplayName("Debe reactivar un usuario inactivo y emitir UsuarioReactivadoEvent")
        void debeReactivarUsuario() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.EMPRESA_ADMIN
            );
            usuario.confirmarIdentidad();
            usuario.desactivar();
            usuario.pullDomainEvents();

            usuario.reactivar();

            assertThat(usuario.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
            assertThat(usuario.estaActivo()).isTrue();

            List<DomainEvent> events = usuario.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(UsuarioReactivadoEvent.class);
        }

        @Test
        @DisplayName("Debe rechazar reactivar un usuario que ya está activo")
        void debeRechazarReactivarActivo() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.SUPER_ADMIN
            );
            usuario.confirmarIdentidad();

            assertThatThrownBy(usuario::reactivar)
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("Sólo un usuario inactivo");
        }
    }

    @Nested
    @DisplayName("Modificación de Rol de Usuario")
    class ModificacionRolTests {

        @Test
        @DisplayName("Debe modificar el rol de un usuario activo y emitir RolUsuarioModificadoEvent")
        void debeModificarRol() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.CAJERO
            );
            usuario.confirmarIdentidad();
            usuario.pullDomainEvents();

            usuario.cambiarRol(RolUsuario.SUCURSAL_MANAGER);

            assertThat(usuario.getRol()).isEqualTo(RolUsuario.SUCURSAL_MANAGER);

            List<DomainEvent> events = usuario.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(RolUsuarioModificadoEvent.class);

            RolUsuarioModificadoEvent event = (RolUsuarioModificadoEvent) events.get(0);
            assertThat(event.rolAnterior()).isEqualTo("CAJERO");
            assertThat(event.nuevoRol()).isEqualTo("SUCURSAL_MANAGER");
        }

        @Test
        @DisplayName("Debe rechazar modificar el rol de un usuario inactivo")
        void debeRechazarModificarRolUsuarioInactivo() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.CAJERO
            );
            usuario.confirmarIdentidad();
            usuario.desactivar();

            assertThatThrownBy(() -> usuario.cambiarRol(RolUsuario.EMPRESA_ADMIN))
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("No se puede modificar el rol de un usuario no activo");
        }

        @Test
        @DisplayName("Debe rechazar asignar el mismo rol actual")
        void debeRechazarAsignarMismoRol() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.BODEGA_OPERATOR
            );
            usuario.confirmarIdentidad();

            assertThatThrownBy(() -> usuario.cambiarRol(RolUsuario.BODEGA_OPERATOR))
                    .isInstanceOf(UsuarioInvalidoException.class)
                    .hasMessageContaining("ya tiene asignado el rol");
        }

        @Test
        @DisplayName("Debe rechazar rol nulo")
        void debeRechazarRolNulo() {
            Usuario usuario = Usuario.registrar(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.SUPER_ADMIN
            );
            usuario.confirmarIdentidad();

            assertThatThrownBy(() -> usuario.cambiarRol(null))
                    .isInstanceOf(UsuarioInvalidoException.class);
        }
    }

    @Nested
    @DisplayName("Reconstitución de Persistencia")
    class ReconstitucionTests {

        @Test
        @DisplayName("Debe reconstituir el agregado sin emitir Domain Events")
        void debeReconstituirSinEventos() {
            Instant ahora = Instant.now();
            Usuario usuario = Usuario.reconstituir(
                    usuarioId,
                    empresaId,
                    usernameValido,
                    emailValido,
                    RolUsuario.SUPER_ADMIN,
                    EstadoUsuario.ACTIVO,
                    ahora,
                    ahora,
                    true,
                    null,
                    null
            );

            assertThat(usuario.getId()).isEqualTo(usuarioId);
            assertThat(usuario.getRol()).isEqualTo(RolUsuario.SUPER_ADMIN);
            assertThat(usuario.getDomainEvents()).isEmpty();
        }
    }
}
