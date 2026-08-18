package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper.EmpresaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.CambiarEstadoSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoSucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Alterna el estado de una Sucursal entre ACTIVA e INACTIVA (SUC-04).
 * <p>
 * Valida el aislamiento multitenant (MT-02): la Sucursal debe pertenecer al empresaId del token.
 * Aplica la regla EMP-06: solo se puede activar una sucursal si la Empresa está ACTIVA.
 */
@Service
@Transactional
public class CambiarEstadoSucursalService implements CambiarEstadoSucursalUseCase {

    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;

    public CambiarEstadoSucursalService(
            EmpresaRepository empresaRepository,
            SucursalRepository sucursalRepository) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository, "empresaRepository no puede ser null.");
        this.sucursalRepository = Objects.requireNonNull(sucursalRepository, "sucursalRepository no puede ser null.");
    }

    @Override
    public SucursalResponse ejecutar(UUID empresaIdUuid, UUID sucursalIdUuid) {
        Objects.requireNonNull(empresaIdUuid, "empresaId no puede ser null.");
        Objects.requireNonNull(sucursalIdUuid, "sucursalId no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(empresaIdUuid);
        SucursalId sucursalId = SucursalId.de(sucursalIdUuid);

        // 1. Cargar y validar la Empresa (MT-02: garantiza que la Sucursal pertenece al tenant)
        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        // 2. Cargar la Sucursal validando pertenencia al tenant (MT-02)
        Sucursal sucursal = sucursalRepository.buscarPorIdYEmpresaId(sucursalId, empresaId)
                .orElseThrow(() -> new EmpresaInvalidaException(
                        "Sucursal con ID " + sucursalIdUuid + " no encontrada para la empresa " + empresaIdUuid + " (MT-02)."
                ));

        // 3. Alternar estado (toggle): ACTIVA → INACTIVA, INACTIVA → ACTIVA (SUC-04)
        if (sucursal.getEstado() == EstadoSucursal.ACTIVA) {
            // Pasar a INACTIVA
            sucursal.desactivar();
        } else {
            // Pasar a ACTIVA — requiere que la Empresa esté ACTIVA (EMP-06)
            if (empresa.getEstado() != EstadoEmpresa.ACTIVA) {
                throw new EmpresaInvalidaException(
                        "No se pueden activar sucursales de una empresa en estado " + empresa.getEstado() + " (EMP-06)."
                );
            }
            sucursal.activar();
        }

        // 4. Persistir la Sucursal actualizada
        Sucursal persistida = sucursalRepository.guardar(sucursal, empresaId);

        return EmpresaApplicationMapper.toResponse(persistida);
    }
}
