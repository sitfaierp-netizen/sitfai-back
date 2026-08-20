package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.EliminarEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class EliminarEmpresaService implements EliminarEmpresaUseCase {

    private final EmpresaRepository empresaRepository;
    private final EmpresaEventPublisher eventPublisher;

    public EliminarEmpresaService(EmpresaRepository empresaRepository, EmpresaEventPublisher eventPublisher) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void ejecutar(UUID empresaId) {
        EmpresaId id = EmpresaId.de(empresaId);
        Empresa empresa = empresaRepository.buscarPorId(id)
                .orElseThrow(() -> new EmpresaNoEncontradaException(id));

        empresa.eliminarEmpresa();

        empresaRepository.guardar(empresa);
        eventPublisher.publicarTodos(empresa.pullDomainEvents());
    }
}
