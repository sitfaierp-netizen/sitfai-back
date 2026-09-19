package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.CrearBodegaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.RegistrarMovimientoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.BodegaWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.CrearBodegaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.MovimientoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarMovimientoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.mapper.InventarioWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Adaptador de Entrada (Driving Adapter): Controlador REST para la gestión de Bodegas y Stock.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-5).
 * Expone la API en {@code /api/v1/bodegas} interactuando únicamente con los Puertos de Entrada (Use Cases).
 * <p>
 * Reglas validadas:
 * <ul>
 *   <li>REGLA-5: Versionado /api/v1, Web DTOs para request/response, cero exposición de objetos de dominio.</li>
 *   <li>MT-01 / MT-06: Tenant {@code empresaId} inyectado como parámetro vía header / contexto de seguridad.</li>
 * </ul>
 */
@RestController
@RequestMapping("/bodegas")
public class BodegaController {

    private final CrearBodegaUseCase crearBodegaUseCase;
    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;
    private final InventarioWebMapper webMapper;

    public BodegaController(
            CrearBodegaUseCase crearBodegaUseCase,
            RegistrarMovimientoUseCase registrarMovimientoUseCase,
            InventarioWebMapper webMapper) {
        this.crearBodegaUseCase = Objects.requireNonNull(crearBodegaUseCase, "crearBodegaUseCase no puede ser null");
        this.registrarMovimientoUseCase = Objects.requireNonNull(registrarMovimientoUseCase, "registrarMovimientoUseCase no puede ser null");
        this.webMapper = Objects.requireNonNull(webMapper, "webMapper no puede ser null");
    }

    /**
     * Endpoint para crear una nueva Bodega.
     * POST /api/v1/bodegas
     */
    @PostMapping
    public ResponseEntity<BodegaWebResponse> crearBodega(
            @RequestHeader(value = "X-Empresa-Id", required = true) String empresaId,
            @RequestBody CrearBodegaWebRequest request) {

        BodegaResponse response = crearBodegaUseCase.ejecutar(webMapper.toCommand(empresaId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toWebResponse(response));
    }

    /**
     * Endpoint para registrar un movimiento de inventario (ENTRADA o SALIDA).
     * POST /api/v1/bodegas/{id}/movimientos
     */
    @PostMapping("/{id}/movimientos")
    public ResponseEntity<MovimientoWebResponse> registrarMovimiento(
            @PathVariable("id") String bodegaId,
            @RequestHeader(value = "X-Empresa-Id", required = true) String empresaId,
            @RequestBody RegistrarMovimientoWebRequest request) {

        MovimientoResponse response = registrarMovimientoUseCase.ejecutar(webMapper.toCommand(empresaId, bodegaId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toWebResponse(response));
    }

    // El endpoint de consulta de stock fue movido a InventoryQueryController para soportar CQRS puro
}
