package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.ActualizarEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper.EmpresaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.ActualizarEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class ActualizarEmpresaService implements ActualizarEmpresaUseCase {

    private final EmpresaRepository empresaRepository;
    private final EmpresaEventPublisher eventPublisher;

    public ActualizarEmpresaService(EmpresaRepository empresaRepository, EmpresaEventPublisher eventPublisher) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public EmpresaResponse ejecutar(ActualizarEmpresaCommand command) {
        EmpresaId id = EmpresaId.de(command.empresaId());
        Empresa empresa = empresaRepository.buscarPorId(id)
                .orElseThrow(() -> new EmpresaNoEncontradaException(id));

        empresa.actualizarEmpresa(new Ruc(command.ruc()), new NombreEmpresa(command.razonSocial()));

        empresaRepository.guardar(empresa);
        eventPublisher.publicarTodos(empresa.pullDomainEvents());

        return EmpresaApplicationMapper.toResponse(empresa);
    }
}
