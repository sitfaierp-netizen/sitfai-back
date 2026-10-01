package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CrearOrdenService implements CrearOrdenUseCase {

    private final OrdenCompraApplicationOperation operation;
    private final ActorProviderPort actorProviderPort;
    private final CurrentTenantProvider currentTenantProvider;

    public CrearOrdenService(OrdenCompraApplicationOperation operation, ActorProviderPort actorProviderPort,
                             CurrentTenantProvider currentTenantProvider) {
        this.operation = operation;
        this.actorProviderPort = actorProviderPort;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    @Transactional
    public OrdenCompraResponse crearBorrador(CrearBorradorCommand command) {
        UUID empresaId = currentTenantProvider.authorizeTenant(command.empresaId());

        return operation.crearBorrador(
                empresaId,
                command.proveedorId(),
                actorProviderPort.getCurrentActorId()
        );
    }
}
