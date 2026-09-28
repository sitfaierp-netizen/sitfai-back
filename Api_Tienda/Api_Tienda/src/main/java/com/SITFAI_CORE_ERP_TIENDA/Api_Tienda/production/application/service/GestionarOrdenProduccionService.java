package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.CompletarProduccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.IniciarProduccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.PlanificarOrdenCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.input.GestionarOrdenProduccionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.OrdenProduccion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.port.output.OrdenProduccionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class GestionarOrdenProduccionService implements GestionarOrdenProduccionUseCase {

    private final OrdenProduccionRepository repository;
    private final TenantProviderPort tenantProvider;
    private final ApplicationEventPublisher eventPublisher;

    public GestionarOrdenProduccionService(OrdenProduccionRepository repository, TenantProviderPort tenantProvider, ApplicationEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public UUID planificarOrden(PlanificarOrdenCommand command) {
        EmpresaId empresaId = new EmpresaId(tenantProvider.getEmpresaIdAutenticada().valor());
        OrdenProduccionId ordenId = new OrdenProduccionId(UUID.randomUUID());
        RecetaId recetaId = new RecetaId(command.recetaId());
        BodegaId bodegaId = new BodegaId(command.bodegaId());
        CantidadProducir cantidad = new CantidadProducir(command.cantidadProducir());

        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);
        repository.guardar(orden, empresaId);

        return ordenId.valor();
    }

    @Override
    public void iniciarProduccion(IniciarProduccionCommand command) {
        EmpresaId empresaId = new EmpresaId(tenantProvider.getEmpresaIdAutenticada().valor());
        OrdenProduccionId ordenId = new OrdenProduccionId(command.ordenId());

        OrdenProduccion orden = repository.buscarPorId(ordenId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Orden de producción no encontrada"));

        orden.iniciarProduccion();
        repository.guardar(orden, empresaId);

        orden.obtenerEventosDominio().forEach(eventPublisher::publishEvent);
        orden.limpiarEventosDominio();
    }

    @Override
    public void completarProduccion(CompletarProduccionCommand command) {
        EmpresaId empresaId = new EmpresaId(tenantProvider.getEmpresaIdAutenticada().valor());
        OrdenProduccionId ordenId = new OrdenProduccionId(command.ordenId());

        OrdenProduccion orden = repository.buscarPorId(ordenId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Orden de producción no encontrada"));

        orden.completarProduccion();
        repository.guardar(orden, empresaId);

        orden.obtenerEventosDominio().forEach(eventPublisher::publishEvent);
        orden.limpiarEventosDominio();
    }
}
