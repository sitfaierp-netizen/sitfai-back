package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.GestionarLineasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GestionarLineasService implements GestionarLineasUseCase {

    private final OrdenCompraApplicationOperation operation;
    private final ActorProviderPort actorProviderPort;
    private final CurrentTenantProvider currentTenantProvider;

    public GestionarLineasService(OrdenCompraApplicationOperation operation, ActorProviderPort actorProviderPort,
                                  CurrentTenantProvider currentTenantProvider) {
        this.operation = operation;
        this.actorProviderPort = actorProviderPort;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    @Transactional
    public OrdenCompraResponse agregarLinea(AgregarLineaCommand command) {
        UUID empresaId = currentTenantProvider.authorizeTenant(command.empresaId());

        return operation.agregarLinea(
                command.ordenCompraId(),
                empresaId,
                command.productoId(),
                command.cantidad(),
                command.costoUnitario(),
                actorProviderPort.getCurrentActorId()
        );
    }
}
