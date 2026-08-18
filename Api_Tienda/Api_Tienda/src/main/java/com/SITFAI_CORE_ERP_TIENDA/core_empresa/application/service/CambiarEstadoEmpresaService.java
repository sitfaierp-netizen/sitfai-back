package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.DarDeBajaEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SuspenderEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper.EmpresaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.CambiarEstadoEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Orquesta las transiciones de estado de una Empresa (EMP-04, EMP-06, EMP-07).
 */
@Service
@Transactional
public class CambiarEstadoEmpresaService implements CambiarEstadoEmpresaUseCase {

    private final EmpresaRepository empresaRepository;
    private final EmpresaEventPublisher eventPublisher;

    public CambiarEstadoEmpresaService(EmpresaRepository empresaRepository, EmpresaEventPublisher eventPublisher) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository, "empresaRepository no puede ser null.");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher no puede ser null.");
    }

    @Override
    public EmpresaResponse suspender(SuspenderEmpresaCommand command) {
        Objects.requireNonNull(command, "command no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        empresa.suspender(command.motivo());

        empresaRepository.guardar(empresa);
        eventPublisher.publicarTodos(empresa.pullDomainEvents());

        return EmpresaApplicationMapper.toResponse(empresa);
    }

    @Override
    public EmpresaResponse darDeBaja(DarDeBajaEmpresaCommand command) {
        Objects.requireNonNull(command, "command no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        empresa.darDeBaja(command.motivo());

        empresaRepository.guardar(empresa);
        eventPublisher.publicarTodos(empresa.pullDomainEvents());

        return EmpresaApplicationMapper.toResponse(empresa);
    }

    @Override
    public EmpresaResponse reactivar(UUID id) {
        Objects.requireNonNull(id, "empresaId no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(id);
        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        empresa.reactivar();

        empresaRepository.guardar(empresa);
        eventPublisher.publicarTodos(empresa.pullDomainEvents());

        return EmpresaApplicationMapper.toResponse(empresa);
    }
}
