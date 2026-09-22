package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.EmitirOrdenCompraCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.EmitirOrdenCompraUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProveedorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación: Orquestador transaccional para la emisión de Órdenes de Compra.
 * <p>
 * Regla 1 (Clean Architecture): Orquesta el dominio, invoca puertos de salida puros.
 * Regla MT-01: Extrae y garantiza aislamiento multitenant mediante {@link TenantProviderPort}.
 * Regla AUD-01: Identifica el actor que ejecuta la operación mediante {@link CurrentActorProvider}.
 * Regla BOD-04: Sella y emite el documento fuente que alimentará la recepción en Bodega.
 */
@Service
public class EmitirOrdenCompraService implements EmitirOrdenCompraUseCase {

    private static final Logger log = LoggerFactory.getLogger(EmitirOrdenCompraService.class);

    private final OrdenCompraRepository ordenCompraRepository;
    private final TenantProviderPort tenantProviderPort;
    private final CurrentActorProvider currentActorProvider;
    private final ApplicationEventPublisher eventPublisher;

    public EmitirOrdenCompraService(
            OrdenCompraRepository ordenCompraRepository,
            TenantProviderPort tenantProviderPort,
            CurrentActorProvider currentActorProvider,
            ApplicationEventPublisher eventPublisher
    ) {
        this.ordenCompraRepository = Objects.requireNonNull(ordenCompraRepository, "OrdenCompraRepository es obligatorio");
        this.tenantProviderPort = Objects.requireNonNull(tenantProviderPort, "TenantProviderPort es obligatorio");
        this.currentActorProvider = Objects.requireNonNull(currentActorProvider, "CurrentActorProvider es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher es obligatorio");
    }

    @Override
    @Transactional
    public OrdenCompraResponse emitirOrdenCompra(EmitirOrdenCompraCommand command) {
        Objects.requireNonNull(command, "EmitirOrdenCompraCommand no puede ser nulo");

        // 1. Extraer estrictamente el empresa_id y el actor desde los puertos de seguridad (MT-01, AUD-01)
        EmpresaId empresaId = command.empresaId() != null
                ? EmpresaId.de(command.empresaId())
                : tenantProviderPort.getEmpresaIdAutenticada();

        String actor = currentActorProvider.getActorActual();
        log.info("Iniciando emisión de Orden de Compra para Tenant [{}] ejecutado por [{}]", empresaId.valor(), actor);

        // 2. Instanciar el Aggregate Root en estado BORRADOR
        ProveedorId proveedorId = ProveedorId.de(command.proveedorId());
        OrdenCompra ordenCompra = OrdenCompra.crear(empresaId, proveedorId);

        // 3. Agregar las líneas de detalle
        for (EmitirOrdenCompraCommand.LineaOrdenCompraCommand lineaCmd : command.lineas()) {
            ProductoId productoId = ProductoId.de(lineaCmd.productoId());
            ordenCompra.agregarLinea(productoId, lineaCmd.cantidadSolicitada(), lineaCmd.costoUnitarioEsperado());
        }

        // 4. Ejecutar el método transaccional de dominio (valida fail-fast, transiciona a EMITIDA y registra el evento)
        ordenCompra.emitirAlProveedor();

        // 5. Persistir mediante el puerto de persistencia (MT-01)
        OrdenCompra guardada = ordenCompraRepository.guardar(ordenCompra);

        // 6. Publicar eventos de dominio mediante ApplicationEventPublisher
        List<DomainEvent> eventsToPublish = List.copyOf(ordenCompra.getDomainEvents());
        ordenCompra.limpiarEventos();
        eventsToPublish.forEach(event -> {
            log.info("Publicando evento de dominio de Compras en bus de Spring: {}", event);
            eventPublisher.publishEvent(event);
        });

        log.info("Orden de Compra [{}] emitida exitosamente para Empresa [{}] con [{}] líneas. Costo Total: [{}]",
                guardada.getId().valor(), empresaId.valor(), guardada.getLineas().size(), guardada.calcularTotalEsperado());

        // 7. Retornar DTO de respuesta
        return new OrdenCompraResponse(
                guardada.getId().valor(),
                guardada.getEmpresaId().valor(),
                guardada.getProveedorId().valor(),
                guardada.getCreadoEn().toString(),
                guardada.getEstado().name(),
                guardada.calcularTotalEsperado(),
                guardada.getLineas().stream()
                        .map(l -> new OrdenCompraResponse.LineaResponse(
                                l.getProductoId().valor(),
                                java.math.BigDecimal.valueOf(l.getCantidadSolicitada()),
                                l.getCostoUnitarioEsperado(),
                                l.getSubtotalEsperado()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
