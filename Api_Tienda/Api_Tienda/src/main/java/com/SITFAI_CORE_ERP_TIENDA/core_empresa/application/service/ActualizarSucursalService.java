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

    public ActualizarSucursalService(EmpresaRepository empresaRepository, EmpresaEventPublisher eventPublisher) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public SucursalResponse ejecutar(ActualizarSucursalCommand command) {
        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        SucursalId sucursalId = SucursalId.de(command.sucursalId());

        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        empresa.actualizarSucursal(sucursalId, command.codigo(), command.nombre());

        empresaRepository.guardar(empresa);
        eventPublisher.publicarTodos(empresa.pullDomainEvents());

        Sucursal sucursalActualizada = empresa.getSucursales().stream()
                .filter(s -> s.getId().equals(sucursalId))
                .findFirst()
                .orElseThrow();

        return EmpresaApplicationMapper.toResponse(sucursalActualizada);
    }
}
