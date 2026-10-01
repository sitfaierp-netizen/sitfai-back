package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.EliminarSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalEliminadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class EliminarSucursalService implements EliminarSucursalUseCase {

    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;
    private final EmpresaEventPublisher eventPublisher;

    public EliminarSucursalService(
            EmpresaRepository empresaRepository,
            SucursalRepository sucursalRepository,
            EmpresaEventPublisher eventPublisher) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository);
        this.sucursalRepository = Objects.requireNonNull(sucursalRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void ejecutar(UUID empresaIdParam, UUID sucursalIdParam) {
        EmpresaId empresaId = EmpresaId.de(empresaIdParam);
        SucursalId sucursalId = SucursalId.de(sucursalIdParam);

        if (!empresaRepository.existe(empresaId)) {
            throw new EmpresaNoEncontradaException(empresaId);
        }

        Sucursal sucursal = sucursalRepository.buscarPorIdYEmpresaId(sucursalId, empresaId)
                .orElseThrow(() -> new EmpresaInvalidaException("Sucursal no encontrada."));
        sucursal.eliminar();
        sucursalRepository.guardar(sucursal, empresaId);
        eventPublisher.publicarTodos(java.util.List.of(SucursalEliminadaEvent.ahora(empresaId, sucursalId)));
    }
}
