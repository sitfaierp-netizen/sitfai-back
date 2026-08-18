package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper.EmpresaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.ObtenerSucursalesPorEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Retorna las Sucursales de una Empresa.
 * <p>
 * Garantiza el aislamiento multitenant (Regla MT-02): solo se devuelven Sucursales
 * del tenant cuyo {@code empresaId} fue recibido como parámetro.
 * No expone datos de otras Empresas.
 */
@Service
@Transactional(readOnly = true)
public class ObtenerSucursalesPorEmpresaService implements ObtenerSucursalesPorEmpresaUseCase {

    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;

    public ObtenerSucursalesPorEmpresaService(
            EmpresaRepository empresaRepository,
            SucursalRepository sucursalRepository) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository, "empresaRepository no puede ser null.");
        this.sucursalRepository = Objects.requireNonNull(sucursalRepository, "sucursalRepository no puede ser null.");
    }

    @Override
    public List<SucursalResponse> ejecutar(UUID empresaIdUuid) {
        Objects.requireNonNull(empresaIdUuid, "empresaId no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(empresaIdUuid);

        // Validar que la Empresa existe antes de consultar sus sucursales (MT-02)
        if (!empresaRepository.existe(empresaId)) {
            throw new EmpresaNoEncontradaException(empresaId);
        }

        return sucursalRepository.buscarPorEmpresaId(empresaId)
                .stream()
                .map(EmpresaApplicationMapper::toResponse)
                .toList();
    }
}
