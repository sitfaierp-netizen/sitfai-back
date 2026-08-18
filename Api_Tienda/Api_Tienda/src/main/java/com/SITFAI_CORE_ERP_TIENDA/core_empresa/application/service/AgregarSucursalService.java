package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.AgregarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper.EmpresaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.AgregarSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio de Aplicación: Orquesta la adición de nuevas sucursales a una empresa existente (SUC-01, SUC-02, EMP-06).
 */
@Service
@Transactional
public class AgregarSucursalService implements AgregarSucursalUseCase {

    private final EmpresaRepository empresaRepository;
    private final EmpresaEventPublisher eventPublisher;

    public AgregarSucursalService(EmpresaRepository empresaRepository, EmpresaEventPublisher eventPublisher) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository, "empresaRepository no puede ser null.");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher no puede ser null.");
    }

    @Override
    public SucursalResponse ejecutar(AgregarSucursalCommand command) {
        Objects.requireNonNull(command, "command no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        Sucursal nuevaSucursal = empresa.agregarSucursal(command.codigo(), command.nombre());

        empresaRepository.guardar(empresa);
        eventPublisher.publicarTodos(empresa.pullDomainEvents());

        return EmpresaApplicationMapper.toResponse(nuevaSucursal);
    }
}
