package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.RegistrarEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.RegistrarEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository;
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
    private final SucursalRepository sucursalRepository;

    public RegistrarEmpresaService(
            EmpresaRepository repository,
            EmpresaEventPublisher eventPublisher,
            SucursalRepository sucursalRepository) {
        this.repository = Objects.requireNonNull(repository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.sucursalRepository = Objects.requireNonNull(sucursalRepository);
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

        // EmpresaJpaAdapter intentionally persists only the aggregate root. Persist
        // the mandatory matrix branch in the same transaction so every downstream
        // tenant reference (Inventory/POS/etc.) points to an owned branch.
        nuevaEmpresa.getSucursales().forEach(
                sucursal -> sucursalRepository.guardar(sucursal, nuevaEmpresaId));

        // Se extraen y publican los eventos de dominio (Ej. EmpresaCreadaEvent - Big Bang)
        // MUST extract from nuevaEmpresa because repository.guardar() returns a reconstituted instance without the events
        eventPublisher.publicarTodos(nuevaEmpresa.pullDomainEvents());

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
