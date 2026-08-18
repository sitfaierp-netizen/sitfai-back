package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.LineaPedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.RemoverLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CancelarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConfirmarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConsultarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CrearPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.GestionarLineasPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.AgregarLineaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CancelarPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CrearPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.PedidoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.mapper.PedidoWebMapper;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Infraestructura Web: PedidoController y TiendaExceptionHandler (Zero Trust @TenantId)")
class PedidoControllerTest {

    @Mock
    private CrearPedidoUseCase crearPedidoUseCase;

    @Mock
    private GestionarLineasPedidoUseCase gestionarLineasUseCase;

    @Mock
    private ConfirmarPedidoUseCase confirmarPedidoUseCase;

    @Mock
    private CancelarPedidoUseCase cancelarPedidoUseCase;

    @Mock
    private ConsultarPedidoUseCase consultarPedidoUseCase;

    private PedidoWebMapper webMapper;
    private PedidoController controller;
    private TiendaExceptionHandler exceptionHandler;

    private final UUID empresaId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final UUID pedidoId = UUID.randomUUID();
    private final UUID lineaId = UUID.randomUUID();
    private final UUID productoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        webMapper = new PedidoWebMapper();
        controller = new PedidoController(
                crearPedidoUseCase,
                gestionarLineasUseCase,
                confirmarPedidoUseCase,
                cancelarPedidoUseCase,
                consultarPedidoUseCase,
                webMapper
        );
        exceptionHandler = new TiendaExceptionHandler();
    }

    @Nested
    @DisplayName("Endpoints REST (Driving Ports)")
    class EndpointsRest {

        @Test
        @DisplayName("POST /api/v1/pedidos: Debe crear un pedido y responder 201 CREATED")
        void debeCrearPedidoExitosamente() {
            CrearPedidoWebRequest request = new CrearPedidoWebRequest(clienteId);
            PedidoResponse appResponse = new PedidoResponse(
                    pedidoId,
                    empresaId,
                    clienteId,
                    "CREADO",
                    BigDecimal.ZERO.setScale(2),
                    "USD",
                    List.of(),
                    Instant.now(),
                    Instant.now()
            );

            when(crearPedidoUseCase.ejecutar(any(CrearPedidoCommand.class))).thenReturn(appResponse);

            ResponseEntity<PedidoWebResponse> response = controller.crearPedido(empresaId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(pedidoId);
            assertThat(response.getBody().empresaId()).isEqualTo(empresaId);
            assertThat(response.getBody().estado()).isEqualTo("CREADO");
            verify(crearPedidoUseCase).ejecutar(new CrearPedidoCommand(empresaId, clienteId));
        }

        @Test
        @DisplayName("POST /api/v1/pedidos/{id}/lineas: Debe agregar una línea y responder 200 OK")
        void debeAgregarLineaExitosamente() {
            AgregarLineaWebRequest request = new AgregarLineaWebRequest(
                    productoId,
                    2,
                    new BigDecimal("19.99"),
                    "USD"
            );

            LineaPedidoResponse lineaResponse = new LineaPedidoResponse(
                    lineaId,
                    productoId,
                    2,
                    new BigDecimal("19.99"),
                    "USD",
                    new BigDecimal("39.98")
            );

            PedidoResponse appResponse = new PedidoResponse(
                    pedidoId,
                    empresaId,
                    clienteId,
                    "CREADO",
                    new BigDecimal("39.98"),
                    "USD",
                    List.of(lineaResponse),
                    Instant.now(),
                    Instant.now()
            );

            when(gestionarLineasUseCase.agregarLinea(any(AgregarLineaPedidoCommand.class))).thenReturn(appResponse);

            ResponseEntity<PedidoWebResponse> response = controller.agregarLinea(empresaId, pedidoId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(pedidoId);
            assertThat(response.getBody().total()).isEqualByComparingTo(new BigDecimal("39.98"));
            assertThat(response.getBody().lineas()).hasSize(1);
            assertThat(response.getBody().lineas().get(0).cantidad()).isEqualTo(2);
            verify(gestionarLineasUseCase).agregarLinea(new AgregarLineaPedidoCommand(
                    empresaId, pedidoId, productoId, 2, new BigDecimal("19.99"), "USD"
            ));
        }

        @Test
        @DisplayName("DELETE /api/v1/pedidos/{id}/lineas/{lineaId}: Debe remover una línea y responder 200 OK")
        void debeRemoverLineaExitosamente() {
            PedidoResponse appResponse = new PedidoResponse(
                    pedidoId,
                    empresaId,
                    clienteId,
                    "CREADO",
                    BigDecimal.ZERO.setScale(2),
                    "USD",
                    List.of(),
                    Instant.now(),
                    Instant.now()
            );

            when(gestionarLineasUseCase.removerLinea(any(RemoverLineaPedidoCommand.class))).thenReturn(appResponse);

            ResponseEntity<PedidoWebResponse> response = controller.removerLinea(empresaId, pedidoId, lineaId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().lineas()).isEmpty();
            verify(gestionarLineasUseCase).removerLinea(new RemoverLineaPedidoCommand(empresaId, pedidoId, lineaId));
        }

        @Test
        @DisplayName("PATCH /api/v1/pedidos/{id}/confirmar: Debe confirmar pedido y responder 200 OK")
        void debeConfirmarPedidoExitosamente() {
            PedidoResponse appResponse = new PedidoResponse(
                    pedidoId,
                    empresaId,
                    clienteId,
                    "CONFIRMADO",
                    new BigDecimal("50.00"),
                    "USD",
                    List.of(),
                    Instant.now(),
                    Instant.now()
            );

            when(confirmarPedidoUseCase.ejecutar(any(ConfirmarPedidoCommand.class))).thenReturn(appResponse);

            ResponseEntity<PedidoWebResponse> response = controller.confirmarPedido(empresaId, pedidoId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(pedidoId);
            assertThat(response.getBody().estado()).isEqualTo("CONFIRMADO");
            verify(confirmarPedidoUseCase).ejecutar(new ConfirmarPedidoCommand(empresaId, pedidoId));
        }

        @Test
        @DisplayName("PATCH /api/v1/pedidos/{id}/cancelar: Debe cancelar pedido y responder 200 OK")
        void debeCancelarPedidoExitosamente() {
            CancelarPedidoWebRequest request = new CancelarPedidoWebRequest("Cliente desistió de la compra");
            PedidoResponse appResponse = new PedidoResponse(
                    pedidoId,
                    empresaId,
                    clienteId,
                    "CANCELADO",
                    new BigDecimal("50.00"),
                    "USD",
                    List.of(),
                    Instant.now(),
                    Instant.now()
            );

            when(cancelarPedidoUseCase.ejecutar(any(CancelarPedidoCommand.class))).thenReturn(appResponse);

            ResponseEntity<PedidoWebResponse> response = controller.cancelarPedido(empresaId, pedidoId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().estado()).isEqualTo("CANCELADO");
            verify(cancelarPedidoUseCase).ejecutar(new CancelarPedidoCommand(empresaId, pedidoId, "Cliente desistió de la compra"));
        }

        @Test
        @DisplayName("GET /api/v1/pedidos/{id}: Debe consultar pedido por ID y responder 200 OK")
        void debeConsultarPedidoPorId() {
            PedidoResponse appResponse = new PedidoResponse(
                    pedidoId,
                    empresaId,
                    clienteId,
                    "CREADO",
                    BigDecimal.ZERO,
                    "USD",
                    List.of(),
                    Instant.now(),
                    Instant.now()
            );

            when(consultarPedidoUseCase.porId(pedidoId, empresaId)).thenReturn(appResponse);

            ResponseEntity<PedidoWebResponse> response = controller.consultarPorId(empresaId, pedidoId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(pedidoId);
            verify(consultarPedidoUseCase).porId(pedidoId, empresaId);
        }

        @Test
        @DisplayName("GET /api/v1/pedidos: Debe listar pedidos por empresa y responder 200 OK")
        void debeListarPedidosPorEmpresa() {
            PedidoResponse appResponse = new PedidoResponse(
                    pedidoId,
                    empresaId,
                    clienteId,
                    "CREADO",
                    BigDecimal.ZERO,
                    "USD",
                    List.of(),
                    Instant.now(),
                    Instant.now()
            );

            when(consultarPedidoUseCase.porEmpresa(empresaId)).thenReturn(List.of(appResponse));

            ResponseEntity<List<PedidoWebResponse>> response = controller.listarPorEmpresa(empresaId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(consultarPedidoUseCase).porEmpresa(empresaId);
        }
    }

    @Nested
    @DisplayName("TiendaExceptionHandler (RFC 7807 Problem Details)")
    class ManejoErroresRfc7807 {

        @Test
        @DisplayName("Debe mapear PedidoInvalidoException a RFC 7807 (422 UNPROCESSABLE_ENTITY)")
        void debeManejarPedidoInvalidoException() {
            PedidoInvalidoException ex = new PedidoInvalidoException("Un pedido sin líneas no puede ser confirmado.");

            ResponseEntity<ProblemDetail> response = exceptionHandler.handlePedidoInvalido(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Violación de Invariante de Pedido");
            assertThat(response.getBody().getStatus()).isEqualTo(422);
            assertThat(response.getBody().getProperties()).containsEntry("codigoError", "ERR_PEDIDO_INVALIDO");
        }

        @Test
        @DisplayName("Debe mapear PedidoNoEncontradoException a RFC 7807 (404 NOT_FOUND)")
        void debeManejarPedidoNoEncontradoException() {
            PedidoNoEncontradoException ex = new PedidoNoEncontradoException(PedidoId.de(pedidoId), EmpresaId.de(empresaId));

            ResponseEntity<ProblemDetail> response = exceptionHandler.handlePedidoNoEncontrado(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Pedido No Encontrado");
            assertThat(response.getBody().getStatus()).isEqualTo(404);
        }
    }
}
