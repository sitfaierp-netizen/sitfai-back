package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.CambiarEstadoSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.ConsultarEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.ObtenerSucursalesPorEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.RegistrarEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.RegistrarEmpresaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.SucursalWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.EmpresaWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.mapper.EmpresaWebMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.RegistrarEmpresaCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Adaptador de entrada REST para el Bounded Context Core-Empresa.
 * <p>
 * Regla 5 (Architecture Rules): NUNCA devuelve objetos del dominio. Solo devuelve Web Response DTOs.
 * El prefijo /api/v1 es delegado al context-path global (no está en @RequestMapping).
 */
@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    private final RegistrarEmpresaUseCase registrarEmpresaUseCase;
    private final ConsultarEmpresaUseCase consultarEmpresaUseCase;
    private final ObtenerSucursalesPorEmpresaUseCase obtenerSucursalesPorEmpresaUseCase;
    private final CambiarEstadoSucursalUseCase cambiarEstadoSucursalUseCase;

    public EmpresaController(
            RegistrarEmpresaUseCase registrarEmpresaUseCase,
            ConsultarEmpresaUseCase consultarEmpresaUseCase,
            ObtenerSucursalesPorEmpresaUseCase obtenerSucursalesPorEmpresaUseCase,
            CambiarEstadoSucursalUseCase cambiarEstadoSucursalUseCase) {
        this.registrarEmpresaUseCase = Objects.requireNonNull(registrarEmpresaUseCase, "registrarEmpresaUseCase no puede ser null.");
        this.consultarEmpresaUseCase = Objects.requireNonNull(consultarEmpresaUseCase, "consultarEmpresaUseCase no puede ser null.");
        this.obtenerSucursalesPorEmpresaUseCase = Objects.requireNonNull(obtenerSucursalesPorEmpresaUseCase, "obtenerSucursalesPorEmpresaUseCase no puede ser null.");
        this.cambiarEstadoSucursalUseCase = Objects.requireNonNull(cambiarEstadoSucursalUseCase, "cambiarEstadoSucursalUseCase no puede ser null.");
    }

    // =========================================================================
    // ESCRITURA
    // =========================================================================

    /**
     * POST /api/v1/empresas — Registra una nueva Empresa (Tenant Raíz).
     * Solo accesible por SUPER_ADMIN (Regla EMP-05).
     * No recibe @TenantId porque este endpoint ES el creador del Tenant.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmpresaWebResponse> registrarEmpresa(
            @RequestBody RegistrarEmpresaWebRequest request) {
        RegistrarEmpresaCommand command = new RegistrarEmpresaCommand(request.ruc(), request.razonSocial());
        EmpresaWebResponse response = EmpresaWebMapper.toWebResponse(
                registrarEmpresaUseCase.ejecutar(command)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // LECTURA — EMPRESA
    // =========================================================================

    /**
     * GET /api/v1/empresas — Lista todas las Empresas registradas en la plataforma.
     * Acceso restringido a SUPER_ADMIN (gestión global de tenants).
     *
     * @return lista de {@link EmpresaWebResponse}, HTTP 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EmpresaWebResponse>> listarEmpresas() {
        List<EmpresaWebResponse> response = EmpresaWebMapper.toWebResponseList(
                consultarEmpresaUseCase.listarTodas()
        );
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // LECTURA — SUCURSAL
    // =========================================================================

    /**
     * GET /api/v1/empresas/{empresaId}/sucursales — Lista las Sucursales de una Empresa.
     * Garantiza aislamiento multitenant (Regla MT-02): solo retorna sucursales del tenant indicado.
     *
     * @param empresaId UUID de la Empresa propietaria.
     * @return lista de {@link SucursalWebResponse}, HTTP 200 OK.
     */
    @GetMapping("/{empresaId}/sucursales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<List<SucursalWebResponse>> listarSucursalesPorEmpresa(
            @PathVariable UUID empresaId) {
        List<SucursalResponse> sucursales = obtenerSucursalesPorEmpresaUseCase.ejecutar(empresaId);
        List<SucursalWebResponse> response = sucursales.stream()
                .map(EmpresaWebMapper::toWebResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // ACTUALIZACIÓN — ESTADO SUCURSAL
    // =========================================================================

    /**
     * PATCH /api/v1/empresas/{empresaId}/sucursales/{sucursalId}/estado
     * Alterna el estado de la Sucursal: ACTIVA → INACTIVA, INACTIVA → ACTIVA (Regla SUC-04).
     * Valida que la Sucursal pertenezca al tenant indicado (Regla MT-02).
     *
     * @param empresaId  UUID de la Empresa propietaria (validación de tenant).
     * @param sucursalId UUID de la Sucursal a modificar.
     * @return {@link SucursalWebResponse} con el estado resultante, HTTP 200 OK.
     */
    @PatchMapping("/{empresaId}/sucursales/{sucursalId}/estado")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<SucursalWebResponse> cambiarEstadoSucursal(
            @PathVariable UUID empresaId,
            @PathVariable UUID sucursalId) {
        SucursalResponse sucursal = cambiarEstadoSucursalUseCase.ejecutar(empresaId, sucursalId);
        return ResponseEntity.ok(EmpresaWebMapper.toWebResponse(sucursal));
    }
}

