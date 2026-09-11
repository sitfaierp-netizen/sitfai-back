package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CambiarRolUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.DesactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.ReactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RegistrarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.CambiarRolUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ConsultarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.DesactivarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ReactivarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.RegistrarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.CambiarRolUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.DesactivarUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.ReactivarUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Infraestructura Web: UsuarioController y IamExceptionHandler")
class UsuarioControllerTest {

    @Mock
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @Mock
    private DesactivarUsuarioUseCase desactivarUsuarioUseCase;

    @Mock
    private ReactivarUsuarioUseCase reactivarUsuarioUseCase;

    @Mock
    private CambiarRolUsuarioUseCase cambiarRolUsuarioUseCase;

    @Mock
    private ConsultarUsuarioUseCase consultarUsuarioUseCase;

    private UsuarioController controller;
    private IamExceptionHandler exceptionHandler;

    private final UUID empresaId = UUID.randomUUID();
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new UsuarioController(
                registrarUsuarioUseCase,
                desactivarUsuarioUseCase,
                reactivarUsuarioUseCase,
                cambiarRolUsuarioUseCase,
                consultarUsuarioUseCase
        );
        exceptionHandler = new IamExceptionHandler();
    }

    @Nested
    @DisplayName("Endpoints REST")
    class EndpointsRestTests {

        @Test
        @DisplayName("POST /api/v1/iam/usuarios debe registrar usuario y retornar HTTP 201 Created con URI")
        void debeRegistrarUsuario() {
            RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO"
            );

            UsuarioResponse mockResponse = new UsuarioResponse(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO",
                    "ACTIVO",
                    Instant.now(),
                    Instant.now()
            );

            when(registrarUsuarioUseCase.ejecutar(any(RegistrarUsuarioCommand.class))).thenReturn(mockResponse);

            ResponseEntity<UsuarioResponse> response = controller.registrar(empresaId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(usuarioId);
            assertThat(response.getHeaders().getLocation()).isNotNull();
            assertThat(response.getHeaders().getLocation().toString()).contains("/iam/usuarios/" + usuarioId);

            verify(registrarUsuarioUseCase).ejecutar(any(RegistrarUsuarioCommand.class));
        }

        @Test
        @DisplayName("PATCH /api/v1/iam/usuarios/{id}/desactivar debe retornar HTTP 200")
        void debeDesactivarUsuario() {
            DesactivarUsuarioRequest request = new DesactivarUsuarioRequest(empresaId, "Baja");
            UsuarioResponse mockResponse = new UsuarioResponse(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO",
                    "INACTIVO",
                    Instant.now(),
                    Instant.now()
            );

            when(desactivarUsuarioUseCase.ejecutar(any(DesactivarUsuarioCommand.class))).thenReturn(mockResponse);

            ResponseEntity<UsuarioResponse> response = controller.desactivar(empresaId, usuarioId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().estado()).isEqualTo("INACTIVO");

            verify(desactivarUsuarioUseCase).ejecutar(any(DesactivarUsuarioCommand.class));
        }

        @Test
        @DisplayName("PATCH /api/v1/iam/usuarios/{id}/reactivar debe retornar HTTP 200")
        void debeReactivarUsuario() {
            ReactivarUsuarioRequest request = new ReactivarUsuarioRequest(empresaId);
            UsuarioResponse mockResponse = new UsuarioResponse(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO",
                    "ACTIVO",
                    Instant.now(),
                    Instant.now()
            );

            when(reactivarUsuarioUseCase.ejecutar(any(ReactivarUsuarioCommand.class))).thenReturn(mockResponse);

            ResponseEntity<UsuarioResponse> response = controller.reactivar(empresaId, usuarioId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().estado()).isEqualTo("ACTIVO");

            verify(reactivarUsuarioUseCase).ejecutar(any(ReactivarUsuarioCommand.class));
        }

        @Test
        @DisplayName("PATCH /api/v1/iam/usuarios/{id}/rol debe modificar rol y retornar HTTP 200")
        void debeCambiarRol() {
            CambiarRolUsuarioRequest request = new CambiarRolUsuarioRequest(empresaId, "SUCURSAL_MANAGER");
            UsuarioResponse mockResponse = new UsuarioResponse(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero@sitfai.com",
                    "SUCURSAL_MANAGER",
                    "ACTIVO",
                    Instant.now(),
                    Instant.now()
            );

            when(cambiarRolUsuarioUseCase.ejecutar(any(CambiarRolUsuarioCommand.class))).thenReturn(mockResponse);

            ResponseEntity<UsuarioResponse> response = controller.cambiarRol(empresaId, usuarioId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().rol()).isEqualTo("SUCURSAL_MANAGER");

            verify(cambiarRolUsuarioUseCase).ejecutar(any(CambiarRolUsuarioCommand.class));
        }

        @Test
        @DisplayName("GET /api/v1/iam/usuarios/{id} debe obtener usuario por ID")
        void debeObtenerPorId() {
            UsuarioResponse mockResponse = new UsuarioResponse(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO",
                    "ACTIVO",
                    Instant.now(),
                    Instant.now()
            );

            when(consultarUsuarioUseCase.obtenerPorId(empresaId, usuarioId)).thenReturn(mockResponse);

            ResponseEntity<UsuarioResponse> response = controller.obtenerPorId(usuarioId, empresaId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(usuarioId);

            verify(consultarUsuarioUseCase).obtenerPorId(empresaId, usuarioId);
        }

        @Test
        @DisplayName("GET /api/v1/iam/usuarios debe listar usuarios por empresa")
        void debeListarPorEmpresa() {
            UsuarioResponse mockResponse = new UsuarioResponse(
                    usuarioId,
                    empresaId,
                    "cajero01",
                    "cajero@sitfai.com",
                    "CAJERO",
                    "ACTIVO",
                    Instant.now(),
                    Instant.now()
            );

            when(consultarUsuarioUseCase.listarPorEmpresa(empresaId)).thenReturn(List.of(mockResponse));

            ResponseEntity<List<UsuarioResponse>> response = controller.listarPorEmpresa(empresaId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);

            verify(consultarUsuarioUseCase).listarPorEmpresa(empresaId);
        }
    }

    @Nested
    @DisplayName("IamExceptionHandler RFC 7807")
    class ExceptionHandlerTests {

        @Test
        @DisplayName("Debe manejar UsuarioNoEncontradoException con HTTP 404")
        void debeManejarUsuarioNoEncontrado() {
            UsuarioNoEncontradoException ex = new UsuarioNoEncontradoException(
                    UsuarioId.de(usuarioId),
                    EmpresaId.de(empresaId)
            );
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleUsuarioNoEncontrado(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Usuario No Encontrado");
        }

        @Test
        @DisplayName("Debe manejar UsuarioInvalidoException con HTTP 422")
        void debeManejarUsuarioInvalido() {
            UsuarioInvalidoException ex = new UsuarioInvalidoException("Username duplicado");
            ResponseEntity<ProblemDetail> response = exceptionHandler.handleUsuarioInvalido(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Violación de Invariante de Usuario");
        }
    }
}
