package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.RemoverLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CancelarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConfirmarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.ConsultarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CrearPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.GestionarLineasPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.AgregarLineaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CancelarPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CrearPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.PedidoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.mapper.PedidoWebMapper;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST Controller: Adaptador Web de Entrada (Driving Adapter) para el Bounded Context de Api_Tienda (Puerto 8084).
 * <p>
 * Reglas de Arquitectura aplicadas:
 * <ul>
 *   <li>REGLA-4 & REGLA-7: Zero Trust con inyección segura de tenant mediante {@link TenantId} desde JWT.</li>
 *   <li>REGLA-5: API REST versionada (/api/v1/pedidos) y uso exclusivo de Web DTOs desacoplados.</li>
 * </ul>
 */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final CrearPedidoUseCase crearPedidoUseCase;
    private final GestionarLineasPedidoUseCase gestionarLineasUseCase;
    private final ConfirmarPedidoUseCase confirmarPedidoUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;
    private final ConsultarPedidoUseCase consultarPedidoUseCase;
    private final PedidoWebMapper webMapper;

    public PedidoController(
            CrearPedidoUseCase crearPedidoUseCase,
            GestionarLineasPedidoUseCase gestionarLineasUseCase,
            ConfirmarPedidoUseCase confirmarPedidoUseCase,
            CancelarPedidoUseCase cancelarPedidoUseCase,
            ConsultarPedidoUseCase consultarPedidoUseCase,
            PedidoWebMapper webMapper) {
        this.crearPedidoUseCase = Objects.requireNonNull(crearPedidoUseCase, "crearPedidoUseCase es requerido.");
        this.gestionarLineasUseCase = Objects.requireNonNull(gestionarLineasUseCase, "gestionarLineasUseCase es requerido.");
        this.confirmarPedidoUseCase = Objects.requireNonNull(confirmarPedidoUseCase, "confirmarPedidoUseCase es requerido.");
        this.cancelarPedidoUseCase = Objects.requireNonNull(cancelarPedidoUseCase, "cancelarPedidoUseCase es requerido.");
        this.consultarPedidoUseCase = Objects.requireNonNull(consultarPedidoUseCase, "consultarPedidoUseCase es requerido.");
        this.webMapper = Objects.requireNonNull(webMapper, "webMapper es requerido.");
    }

    /**
     * Inicia un nuevo Pedido en estado CREADO.
     */
    @PostMapping
    public ResponseEntity<PedidoWebResponse> crearPedido(
            @TenantId UUID empresaId,
            @RequestBody CrearPedidoWebRequest request) {

        CrearPedidoCommand command = webMapper.toCommand(empresaId, request);
        PedidoResponse response = crearPedidoUseCase.ejecutar(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toWebResponse(response));
    }

    /**
     * Agrega una línea de producto al Pedido.
     */
    @PostMapping("/{id}/lineas")
    public ResponseEntity<PedidoWebResponse> agregarLinea(
            @TenantId UUID empresaId,
            @PathVariable("id") UUID id,
            @RequestBody AgregarLineaWebRequest request) {

        AgregarLineaPedidoCommand command = webMapper.toAgregarLineaCommand(empresaId, id, request);
        PedidoResponse response = gestionarLineasUseCase.agregarLinea(command);

        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    /**
     * Remueve una línea de producto del Pedido.
     */
    @DeleteMapping("/{id}/lineas/{lineaId}")
    public ResponseEntity<PedidoWebResponse> removerLinea(
            @TenantId UUID empresaId,
            @PathVariable("id") UUID id,
            @PathVariable("lineaId") UUID lineaId) {

        RemoverLineaPedidoCommand command = webMapper.toRemoverLineaCommand(empresaId, id, lineaId);
        PedidoResponse response = gestionarLineasUseCase.removerLinea(command);

        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    /**
     * Confirma el Pedido y dispara la orquestación de eventos de dominio (Inventario, Facturación).
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PedidoWebResponse> confirmarPedido(
            @TenantId UUID empresaId,
            @PathVariable("id") UUID id) {

        ConfirmarPedidoCommand command = webMapper.toConfirmarCommand(empresaId, id);
        PedidoResponse response = confirmarPedidoUseCase.ejecutar(command);

        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    /**
     * Cancela el Pedido registrando el motivo.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoWebResponse> cancelarPedido(
            @TenantId UUID empresaId,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) CancelarPedidoWebRequest request) {

        CancelarPedidoCommand command = webMapper.toCancelarCommand(empresaId, id, request);
        PedidoResponse response = cancelarPedidoUseCase.ejecutar(command);

        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    /**
     * Consulta un Pedido por su ID con aislamiento multitenant MT-01.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoWebResponse> consultarPorId(
            @TenantId UUID empresaId,
            @PathVariable("id") UUID id) {

        PedidoResponse response = consultarPedidoUseCase.porId(id, empresaId);

        return ResponseEntity.ok(webMapper.toWebResponse(response));
    }

    /**
     * Lista todos los pedidos de la empresa autenticada.
     */
    @GetMapping
    public ResponseEntity<List<PedidoWebResponse>> listarPorEmpresa(
            @TenantId UUID empresaId) {

        List<PedidoResponse> responses = consultarPedidoUseCase.porEmpresa(empresaId);

        return ResponseEntity.ok(webMapper.toWebResponseList(responses));
    }
}
