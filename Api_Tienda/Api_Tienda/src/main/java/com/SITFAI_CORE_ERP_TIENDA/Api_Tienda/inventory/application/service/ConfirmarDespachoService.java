package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConfirmarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ConfirmarDespachoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.Despacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.LineaDespacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.DespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de Aplicación: Orquestador transaccional para la Confirmación de Despacho (Outbound Logistics).
 * <p>
 * Regla 1 (Clean Architecture): Orquesta el dominio puro y puertos de salida.
 * Regla MT-01: Extrae de forma segura el tenant autenticado mediante TenantProviderPort.
 * Regla BOD-04: Referencia inmutable al Pedido origen como documento fuente.
 */
@Service
public class ConfirmarDespachoService implements ConfirmarDespachoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarDespachoService.class);

    private final DespachoRepository despachoRepository;
    private final TenantProviderPort tenantProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmarDespachoService(
            DespachoRepository despachoRepository,
            TenantProviderPort tenantProviderPort,
            ApplicationEventPublisher eventPublisher) {
        this.despachoRepository = Objects.requireNonNull(despachoRepository, "DespachoRepository es obligatorio");
        this.tenantProviderPort = Objects.requireNonNull(tenantProviderPort, "TenantProviderPort es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher es obligatorio");
    }

    @Override
    @Transactional
    public DespachoResponse confirmarDespacho(ConfirmarDespachoCommand command) {
        Objects.requireNonNull(command, "ConfirmarDespachoCommand no puede ser nulo");

        // 1. Extraer el empresa_id de forma segura mediante los puertos de seguridad (MT-01)
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        DespachoId despachoId = DespachoId.generar();
        PedidoId pedidoId = PedidoId.de(command.pedidoId());
        BodegaId bodegaId = command.bodegaId() != null ? BodegaId.de(command.bodegaId()) : null;

        log.info("Iniciando despacho [{}] para Empresa [{}] bajo Pedido [{}]",
                despachoId.valor(), empresaId.valor(), pedidoId.valor());

        // 2. Instanciar un Despacho en PENDIENTE
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId, bodegaId);

        // 3. Agregar las líneas
        for (ConfirmarDespachoCommand.LineaDespachoCommand lineaCmd : command.lineas()) {
            despacho.agregarLinea(new LineaDespacho(
                    ProductoId.de(lineaCmd.productoId()),
                    Cantidad.de(lineaCmd.cantidad())
            ));
        }

        // 4. Ejecutar la lógica de dominio confirmarDespacho()
        despacho.confirmarDespacho();

        // 5. Extraer eventos de dominio antes de persistir
        List<DomainEvent> domainEvents = despacho.pullDomainEvents();

        // 6. Guardar el agregado usando DespachoRepository
        Despacho guardado = despachoRepository.guardar(despacho, empresaId);

        // 7. Despachar el evento DespachoConfirmadoEvent vía ApplicationEventPublisher
        domainEvents.forEach(eventPublisher::publishEvent);

        log.info("Despacho [{}] confirmado y persistido exitosamente con [{}] líneas.",
                guardado.getId().valor(), guardado.getLineas().size());

        return new DespachoResponse(
                guardado.getId().valor(),
                guardado.getPedidoId().valor(),
                guardado.getBodegaId() != null ? guardado.getBodegaId().valor() : null,
                guardado.getEstado().name(),
                guardado.getLineas().size(),
                guardado.getUpdatedAt(),
                "Despacho confirmado exitosamente"
        );
    }
}
