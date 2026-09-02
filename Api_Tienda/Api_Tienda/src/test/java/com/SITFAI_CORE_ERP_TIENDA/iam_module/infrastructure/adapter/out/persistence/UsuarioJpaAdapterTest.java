package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.EstadoUsuario;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Infraestructura Persistencia: UsuarioJpaAdapter y UsuarioPersistenceMapper")
class UsuarioJpaAdapterTest {

    @Mock
    private UsuarioJpaRepository jpaRepository;

    private UsuarioJpaAdapter jpaAdapter;

    private final UUID empresaUuid = UUID.randomUUID();
    private final UUID usuarioUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        jpaAdapter = new UsuarioJpaAdapter(jpaRepository);
    }

    @Nested
    @DisplayName("Mapeo Bidireccional (UsuarioPersistenceMapper)")
    class MapperTests {

        @Test
        @DisplayName("Debe mapear de Dominio a Entidad JPA y viceversa sin pérdida de información")
        void debeMapearCorrectamente() {
            Usuario usuario = Usuario.registrar(
                    UsuarioId.de(usuarioUuid),
                    EmpresaId.de(empresaUuid),
                    Username.de("cajero01"),
                    Email.de("cajero@sitfai.com"),
                    RolUsuario.CAJERO
            );

            UsuarioJpaEntity entity = UsuarioPersistenceMapper.toJpaEntity(usuario);

            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(usuarioUuid.toString());
            assertThat(entity.getEmpresaId()).isEqualTo(empresaUuid.toString());
            assertThat(entity.getUsername()).isEqualTo("cajero01");
            assertThat(entity.getEmail()).isEqualTo("cajero@sitfai.com");
            assertThat(entity.getRol()).isEqualTo("CAJERO");
            assertThat(entity.getEstado()).isEqualTo("ACTIVO");

            Usuario reconstruido = UsuarioPersistenceMapper.toDomainEntity(entity);

            assertThat(reconstruido).isNotNull();
            assertThat(reconstruido.getId().valor()).isEqualTo(usuarioUuid);
            assertThat(reconstruido.getEmpresaId().valor()).isEqualTo(empresaUuid);
            assertThat(reconstruido.getUsername().valor()).isEqualTo("cajero01");
            assertThat(reconstruido.getEmail().valor()).isEqualTo("cajero@sitfai.com");
            assertThat(reconstruido.getRol()).isEqualTo(RolUsuario.CAJERO);
            assertThat(reconstruido.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
        }
    }

    @Nested
    @DisplayName("Operaciones del Adaptador JPA")
    class AdapterOperationsTests {

        @Test
        @DisplayName("Debe guardar usuario delegando en el repositorio JPA")
        void debeGuardarUsuario() {
            Usuario usuario = Usuario.registrar(
                    UsuarioId.de(usuarioUuid),
                    EmpresaId.de(empresaUuid),
                    Username.de("cajero01"),
                    Email.de("cajero@sitfai.com"),
                    RolUsuario.CAJERO
            );

            UsuarioJpaEntity entity = UsuarioPersistenceMapper.toJpaEntity(usuario);
            when(jpaRepository.save(any(UsuarioJpaEntity.class))).thenReturn(entity);

            Usuario guardado = jpaAdapter.guardar(usuario);

            assertThat(guardado).isNotNull();
            assertThat(guardado.getId().valor()).isEqualTo(usuarioUuid);
            verify(jpaRepository).save(any(UsuarioJpaEntity.class));
        }

        @Test
        @DisplayName("Debe buscar usuario por ID y Tenant")
        void debeBuscarPorId() {
            Instant ahora = Instant.now();
            UsuarioJpaEntity entity = new UsuarioJpaEntity(
                    usuarioUuid.toString(),
                    empresaUuid.toString(),
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO",
                    "ACTIVO",
                    ahora,
                    ahora,
                    true,
                    null,
                    null
            );

            when(jpaRepository.findByEmpresaIdAndId(empresaUuid.toString(), usuarioUuid.toString()))
                    .thenReturn(Optional.of(entity));

            Optional<Usuario> opt = jpaAdapter.buscarPorId(EmpresaId.de(empresaUuid), UsuarioId.de(usuarioUuid));

            assertThat(opt).isPresent();
            assertThat(opt.get().getUsername().valor()).isEqualTo("cajero01");
        }

        @Test
        @DisplayName("Debe verificar existencia de username y email por empresa")
        void debeVerificarExistencia() {
            when(jpaRepository.existsByEmpresaIdAndUsername(empresaUuid.toString(), "cajero01")).thenReturn(true);
            when(jpaRepository.existsByEmpresaIdAndEmail(empresaUuid.toString(), "cajero@sitfai.com")).thenReturn(false);

            boolean existeUsername = jpaAdapter.existePorUsername(EmpresaId.de(empresaUuid), Username.de("cajero01"));
            boolean existeEmail = jpaAdapter.existePorEmail(EmpresaId.de(empresaUuid), Email.de("cajero@sitfai.com"));

            assertThat(existeUsername).isTrue();
            assertThat(existeEmail).isFalse();
        }

        @Test
        @DisplayName("Debe listar usuarios por empresa")
        void debeListarPorEmpresa() {
            Instant ahora = Instant.now();
            UsuarioJpaEntity entity = new UsuarioJpaEntity(
                    usuarioUuid.toString(),
                    empresaUuid.toString(),
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO",
                    "ACTIVO",
                    ahora,
                    ahora,
                    true,
                    null,
                    null
            );

            when(jpaRepository.findByEmpresaId(empresaUuid.toString())).thenReturn(List.of(entity));

            List<Usuario> usuarios = jpaAdapter.buscarPorEmpresa(EmpresaId.de(empresaUuid));

            assertThat(usuarios).hasSize(1);
            assertThat(usuarios.get(0).getUsername().valor()).isEqualTo("cajero01");
        }
    }
}
