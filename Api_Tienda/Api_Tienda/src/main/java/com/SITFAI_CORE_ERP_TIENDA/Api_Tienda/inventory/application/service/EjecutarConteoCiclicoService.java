package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConteoCiclicoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DetalleConteoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.FinalizarConteoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarConteoFisicoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.FinalizarConteoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarConteoFisicoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.ConteoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.ConteoCiclico;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.event.DiscrepanciaInventarioDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.port.ConteoCiclicoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.CantidadFisica;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ProductoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Orquestador transaccional para el registro de conteos físicos
 * y finalización analítica de ciclos de conteo (Auditoría WMS).
 * <p>
 * Regla MT-01: Extrae de forma segura el {@code empresa_id} mediante {@link TenantProviderPort}.
 * Regla REGLA-1: Coordina el Agregado {@link ConteoCiclico} y puertos sin acoplar el dominio a frameworks.
 */
@Service
public class EjecutarConteoCiclicoService implements RegistrarConteoFisicoUseCase, FinalizarConteoUseCase {

    private static final Logger log = LoggerFactory.getLogger(EjecutarConteoCiclicoService.class);

    private final ConteoCiclicoRepository conteoRepository;
    private final TenantProviderPort tenantProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public EjecutarConteoCiclicoService(
            ConteoCiclicoRepository conteoRepository,
            TenantProviderPort tenantProviderPort,
            ApplicationEventPublisher eventPublisher) {
        this.conteoRepository = Objects.requireNonNull(conteoRepository, "ConteoCiclicoRepository es obligatorio.");
        this.tenantProviderPort = Objects.requireNonNull(tenantProviderPort, "TenantProviderPort es obligatorio.");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher es obligatorio.");
    }

    @Override
    @Transactional
    public ConteoCiclicoResponse registrar(RegistrarConteoFisicoCommand command) {
        Objects.requireNonNull(command, "RegistrarConteoFisicoCommand no puede ser nulo.");

        // 1. Extraer empresaId de forma segura (MT-01)
        UUID empresaUuid = tenantProviderPort.getEmpresaIdAutenticada().valor();
        EmpresaId empresaId = new EmpresaId(empresaUuid);
        ConteoId conteoId = ConteoId.de(command.conteoId());

        log.info("WMS: Registrando conteo físico para Conteo [{}] en Empresa [{}]", conteoId, empresaId);

        // 2. Cargar Agregado desde el repositorio
        ConteoCiclico conteo = conteoRepository.buscarPorId(empresaId, conteoId)
                .orElseThrow(() -> new ConteoNoEncontradoException(conteoId, empresaId));

        // 3. Invocar comportamiento de dominio
        conteo.registrarConteoFisico(
                ProductoId.de(command.productoId()),
                CantidadFisica.de(command.cantidadFisica())
        );

        // 4. Guardar cambios
        conteoRepository.guardar(conteo);

        return mapearAResponse(conteo);
    }

    @Override
    @Transactional
    public ConteoCiclicoResponse finalizar(FinalizarConteoCommand command) {
        Objects.requireNonNull(command, "FinalizarConteoCommand no puede ser nulo.");

        // 1. Extraer empresaId de forma segura (MT-01)
        UUID empresaUuid = tenantProviderPort.getEmpresaIdAutenticada().valor();
        EmpresaId empresaId = new EmpresaId(empresaUuid);
        ConteoId conteoId = ConteoId.de(command.conteoId());

        log.info("WMS: Finalizando ciclo de auditoría para Conteo [{}] en Empresa [{}]", conteoId, empresaId);

        // 2. Cargar Agregado
        ConteoCiclico conteo = conteoRepository.buscarPorId(empresaId, conteoId)
                .orElseThrow(() -> new ConteoNoEncontradoException(conteoId, empresaId));

        // 3. Finalizar evaluación de líneas
        conteo.finalizar();

        // 4. Guardar actualización del estado
        conteoRepository.guardar(conteo);

        // 5. Publicar eventos de dominio si se detectaron discrepancias
        List<DiscrepanciaInventarioDetectadaEvent> eventos = conteo.pullDomainEvents();
        eventos.forEach(event -> {
            log.warn("WMS: Discrepancia detectada en Conteo [{}]: {} líneas con diferencias",
                    event.conteoId(), event.discrepancias().size());
            eventPublisher.publishEvent(event);
        });

        return mapearAResponse(conteo);
    }

    private ConteoCiclicoResponse mapearAResponse(ConteoCiclico conteo) {
        List<DetalleConteoResponse> detalles = conteo.getDetalles().stream()
                .map(d -> new DetalleConteoResponse(
                        d.getId(),
                        d.getProductoId().valor(),
                        d.getCantidadTeorica(),
                        d.getCantidadFisica() != null ? d.getCantidadFisica().valor() : null,
                        d.calcularDiferencia(),
                        d.tieneDiscrepancia()
                ))
                .toList();

        int totalDiscrepancias = (int) detalles.stream().filter(DetalleConteoResponse::tieneDiscrepancia).count();

        return new ConteoCiclicoResponse(
                conteo.getId().valor(),
                conteo.getEmpresaId().valor(),
                conteo.getBodegaId().valor(),
                conteo.getEstado().name(),
                conteo.getFechaProgramada(),
                detalles.size(),
                totalDiscrepancias,
                detalles
        );
    }
}
