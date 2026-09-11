package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.ActualizarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper.EmpresaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.ActualizarSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class ActualizarSucursalService implements ActualizarSucursalUseCase {

    private final EmpresaRepository empresaRepository;
    private final EmpresaEventPublisher eventPublisher;
    private final com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository sucursalRepository;

    public ActualizarSucursalService(
            EmpresaRepository empresaRepository, 
            EmpresaEventPublisher eventPublisher,
            com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository sucursalRepository) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.sucursalRepository = Objects.requireNonNull(sucursalRepository);
    }

    @Override
    public SucursalResponse ejecutar(ActualizarSucursalCommand command) {
        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        SucursalId sucursalId = SucursalId.de(command.sucursalId());

        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        Sucursal sucursal = sucursalRepository.buscarPorIdYEmpresaId(sucursalId, empresaId)
                .orElseThrow(() -> new com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException("Sucursal no encontrada o no pertenece a la empresa"));

        if (!sucursal.getCodigo().equalsIgnoreCase(command.codigo().trim())) {
            if (sucursalRepository.existePorEmpresaIdYCodigo(empresaId, command.codigo().trim().toUpperCase())) {
                throw new com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.RegistroDuplicadoException("Sucursal", "codigo", command.codigo());
            }
        }

        sucursal.actualizar(command.codigo(), command.nombre());
        
        Sucursal sucursalActualizada = sucursalRepository.guardar(sucursal, empresaId);
        
        // Disparar evento de dominio de sucursal actualizada, pero necesitamos publicarlo desde Empresa o directamente?
        // Como Empresa es el aggregate root y nosotros usamos el repositorio de sucursal...
        // Replicar comportamiento de evento:
        eventPublisher.publicarTodos(java.util.List.of(
            com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalActualizadaEvent.ahora(empresaId, sucursalId, sucursal.getCodigo(), sucursal.getNombre())
        ));

        return EmpresaApplicationMapper.toResponse(sucursalActualizada);
    }
}
