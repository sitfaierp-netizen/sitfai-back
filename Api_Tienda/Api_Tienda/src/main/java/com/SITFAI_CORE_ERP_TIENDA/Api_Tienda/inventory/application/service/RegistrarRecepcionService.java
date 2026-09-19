package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.LineaRecepcionDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarRecepcionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarRecepcionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.LineaRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.Recepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.RecepcionRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RegistrarRecepcionService implements RegistrarRecepcionUseCase {

    private final RecepcionRepository recepcionRepository;
    private final TenantProviderPort tenantProviderPort;
    private final ActorProviderPort actorProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrarRecepcionService(
            RecepcionRepository recepcionRepository,
            TenantProviderPort tenantProviderPort,
            ActorProviderPort actorProviderPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.recepcionRepository = recepcionRepository;
        this.tenantProviderPort = tenantProviderPort;
        this.actorProviderPort = actorProviderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UUID registrar(RegistrarRecepcionCommand command) {
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        String currentActor = actorProviderPort.getCurrentActorId();

        RecepcionId recepcionId = RecepcionId.generar();
        BodegaId bodegaDestinoId = BodegaId.de(command.bodegaDestinoId());
        OrdenCompraId ordenCompraOrigenId = new OrdenCompraId(command.ordenCompraOrigenId());

        Recepcion recepcion = Recepcion.crearBorrador(
                recepcionId,
                empresaId,
                bodegaDestinoId,
                ordenCompraOrigenId,
                currentActor
        );

        for (LineaRecepcionDto lineaDto : command.lineas()) {
            Lote lote = new Lote(lineaDto.lote().codigoLote(), lineaDto.lote().fechaCaducidad());
            LineaRecepcion linea = new LineaRecepcion(
                    ProductoId.de(lineaDto.productoId()),
                    Cantidad.de(lineaDto.cantidad()),
                    lote
            );
            recepcion.agregarLinea(linea);
        }

        recepcion.confirmarRecepcion();

        recepcionRepository.guardar(recepcion);

        List<DomainEvent> events = recepcion.pullDomainEvents();
        for (DomainEvent event : events) {
            eventPublisher.publishEvent(event);
        }

        return recepcion.getId().valor();
    }
}
