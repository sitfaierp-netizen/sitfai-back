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
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.TenantAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CambiarEstadoOrdenService implements CambiarEstadoOrdenUseCase {

    private final OrdenCompraRepository repository;
    private final OrdenCompraEventPublisher eventPublisher;
    private final ActorProviderPort actorProviderPort;
    private final com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarIngresoStockUseCase registrarIngresoStockUseCase;

    public CambiarEstadoOrdenService(OrdenCompraRepository repository, 
                                     OrdenCompraEventPublisher eventPublisher, 
                                     ActorProviderPort actorProviderPort,
                                     com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarIngresoStockUseCase registrarIngresoStockUseCase) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.actorProviderPort = actorProviderPort;
        this.registrarIngresoStockUseCase = registrarIngresoStockUseCase;
    }

    @Override
    @Transactional
    public OrdenCompraResponse cambiarEstado(CambiarEstadoCommand command) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID empresaId = null;
        if (authentication != null && authentication.getDetails() instanceof TenantAuthenticationDetails details) {
            empresaId = details.empresaUuid();
        } else {
            empresaId = command.empresaId();
        }
        
        if (empresaId == null) {
            throw new IllegalArgumentException("empresa_id no encontrado.");
        }

        OrdenCompra orden = repository.buscarPorIdYEmpresaId(
                new OrdenCompraId(command.ordenCompraId()),
                empresaId
        ).orElseThrow(() -> new DomainException("Orden de Compra no encontrada."));

        orden.setUpdatedBy(actorProviderPort.getCurrentActorId());

        switch (command.nuevoEstado().toUpperCase()) {
            case "EMITIDA" -> orden.emitir();
            case "APROBADA" -> orden.aprobar();
            case "RECIBIDA" -> {
                orden.recibir();
                // Registrar ingreso en Kardex por cada línea
                for (var linea : orden.getLineas()) {
                    com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarIngresoStockCommand ingresoCmd = 
                        new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarIngresoStockCommand(
                            orden.getBodegaDestinoId(),
                            linea.getProductoId().valor(),
                            java.math.BigDecimal.valueOf(linea.getCantidad()),
                            null, // Lote opcional
                            null, // Fecha caducidad
                            "ORDEN_COMPRA",
                            orden.getId().valor().toString()
                    );
                    registrarIngresoStockUseCase.registrarIngreso(ingresoCmd);
                }
            }
            case "ANULADA" -> orden.anular(); // "CANCELADA" se reemplazó por "ANULADA" en DocumentoTransaccional
            default -> throw new IllegalArgumentException("Estado no soportado: " + command.nuevoEstado());
        }

        repository.guardar(orden);

        // Publicar Domain Events
        orden.pullDomainEvents().forEach(eventPublisher::publicar);

        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
