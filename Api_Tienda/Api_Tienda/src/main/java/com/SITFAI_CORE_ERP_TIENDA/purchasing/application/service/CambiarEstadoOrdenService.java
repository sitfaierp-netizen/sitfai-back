package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CambiarEstadoOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CambiarEstadoOrdenService implements CambiarEstadoOrdenUseCase {

    private final OrdenCompraRepository repository;
    private final OrdenCompraEventPublisher eventPublisher;
    private final ActorProviderPort actorProviderPort;
    private final CurrentTenantProvider currentTenantProvider;

    public CambiarEstadoOrdenService(OrdenCompraRepository repository, OrdenCompraEventPublisher eventPublisher,
                                     ActorProviderPort actorProviderPort, CurrentTenantProvider currentTenantProvider) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.actorProviderPort = actorProviderPort;
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    @Transactional
    public OrdenCompraResponse cambiarEstado(CambiarEstadoCommand command) {
        UUID empresaId = currentTenantProvider.authorizeTenant(command.empresaId());

        OrdenCompra orden = repository.buscarPorIdYEmpresaId(
                new OrdenCompraId(command.ordenCompraId()),
                empresaId
        ).orElseThrow(() -> new DomainException("Orden de Compra no encontrada."));

        orden.setUpdatedBy(actorProviderPort.getCurrentActorId());

        switch (command.nuevoEstado().toUpperCase()) {
            case "EMITIDA" -> orden.emitir();
            case "ANULADA" -> orden.anular(); // "CANCELADA" se reemplazó por "ANULADA" en DocumentoTransaccional
            default -> throw new IllegalArgumentException("Estado no soportado: " + command.nuevoEstado());
        }

        repository.guardar(orden);

        // Publicar Domain Events
        orden.pullDomainEvents().forEach(eventPublisher::publicar);

        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
