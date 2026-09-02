package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.RegistrarEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.RegistrarEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class RegistrarEmpresaService implements RegistrarEmpresaUseCase {

    private final EmpresaRepository repository;
    private final EmpresaEventPublisher eventPublisher;

    public RegistrarEmpresaService(EmpresaRepository repository, EmpresaEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    @Transactional
    public EmpresaResponse ejecutar(RegistrarEmpresaCommand command) {
        Objects.requireNonNull(command, "Command no puede ser null");

        Ruc ruc = new Ruc(command.ruc());
        NombreEmpresa nombre = new NombreEmpresa(command.razonSocial());

        if (repository.existePorRuc(ruc)) {
            throw new com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.RegistroDuplicadoException("Empresa", "ruc", ruc.valor());
        }

        EmpresaId nuevaEmpresaId = EmpresaId.generar();

        // Se usa el factory method del Aggregate Root
        Empresa nuevaEmpresa = Empresa.registrar(
                nuevaEmpresaId,
                ruc,
                nombre,
                "MATRIZ",
                "Sucursal Principal Matriz"
        );

        Empresa empresaGuardada = repository.guardar(nuevaEmpresa);

        // Se extraen y publican los eventos de dominio (Ej. EmpresaCreadaEvent - Big Bang)
        eventPublisher.publicarTodos(empresaGuardada.pullDomainEvents());

        return new EmpresaResponse(
                empresaGuardada.getId().valor(),
                empresaGuardada.getRuc().valor(),
                empresaGuardada.getNombre().valor(),
                empresaGuardada.getEstado().name(),
                java.util.Collections.emptyList(),
                empresaGuardada.getCreadoEn(),
                empresaGuardada.getActualizadoEn()
        );
    }
}
