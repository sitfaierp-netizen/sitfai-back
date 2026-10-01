package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CrearOrdenService implements CrearOrdenUseCase {

    private final OrdenCompraRepository repository;
    private final ActorProviderPort actorProviderPort;
    private final CurrentTenantProvider currentTenantProvider;

    public CrearOrdenService(OrdenCompraRepository repository, ActorProviderPort actorProviderPort,
                             CurrentTenantProvider currentTenantProvider) {
        this.repository = repository;
        this.actorProviderPort = actorProviderPort;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    @Transactional
    public OrdenCompraResponse crearBorrador(CrearBorradorCommand command) {
        UUID empresaId = currentTenantProvider.authorizeTenant(command.empresaId());

        String createdBy = actorProviderPort.getCurrentActorId();

        OrdenCompra orden = OrdenCompra.crear(
                OrdenCompraId.generar(),
                empresaId,
                new ProveedorId(command.proveedorId()),
                createdBy
        );

        repository.guardar(orden);
        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
