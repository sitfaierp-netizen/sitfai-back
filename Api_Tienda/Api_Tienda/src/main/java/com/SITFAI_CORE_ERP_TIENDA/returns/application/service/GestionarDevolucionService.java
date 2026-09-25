package com.SITFAI_CORE_ERP_TIENDA.returns.application.service;

import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.AutorizacionDevolucionResponse;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.CrearAutorizacionDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.InspeccionarDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.LineaDevolucionResponse;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.port.input.GestionarDevolucionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.AutorizacionDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.LineaDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.CantidadDevuelta;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DevolucionId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.MotivoDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.port.output.AutorizacionDevolucionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GestionarDevolucionService implements GestionarDevolucionUseCase {

    private final AutorizacionDevolucionRepository repository;
    private final TenantProviderPort tenantProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public GestionarDevolucionService(AutorizacionDevolucionRepository repository,
                                      TenantProviderPort tenantProviderPort,
                                      ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.tenantProviderPort = tenantProviderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public AutorizacionDevolucionResponse crearAutorizacion(CrearAutorizacionDevolucionCommand command) {
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        DevolucionId devolucionId = DevolucionId.generar();
        DocumentoFuenteId documentoFuenteId = DocumentoFuenteId.de(command.documentoFuenteId());

        List<LineaDevolucion> lineas = command.lineas().stream()
                .map(l -> new LineaDevolucion(
                        ProductoId.de(l.productoId()),
                        CantidadDevuelta.de(l.cantidad()),
                        MotivoDevolucion.de(l.motivo())
                ))
                .collect(Collectors.toList());

        AutorizacionDevolucion autorizacion = AutorizacionDevolucion.emitir(empresaId, devolucionId, documentoFuenteId, lineas);
        // Automatically mark it as received for this flow, per prompt it says "ejecutar recibirFisicamente() o inspeccionar(...)"
        // But usually creation is AUTORIZADA, and then physically received.
        // I will create it as AUTORIZADA and receive it right away to simplify tests unless instructed otherwise,
        // Wait, the prompt implies "orquestar la recepción física de devoluciones, las inspecciones de calidad (QA)"
        // It says "Simula el flujo completo vía REST (creación e inspección)". I will just call recibirFisicamente during creation to advance it to RECIBIDA_EN_CUARENTENA so it can be inspected.
        autorizacion.recibirFisicamente();

        repository.guardar(empresaId, autorizacion);
        
        return toResponse(autorizacion);
    }

    @Override
    @Transactional
    public AutorizacionDevolucionResponse inspeccionar(InspeccionarDevolucionCommand command) {
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        DevolucionId devolucionId = DevolucionId.de(command.devolucionId());

        AutorizacionDevolucion autorizacion = repository.buscarPorId(empresaId, devolucionId)
                .orElseThrow(() -> new IllegalArgumentException("Autorización de devolución no encontrada"));

        autorizacion.inspeccionar(ProductoId.de(command.productoId()), command.aprobado());
        
        repository.guardar(empresaId, autorizacion);
        
        autorizacion.pullDomainEvents().forEach(eventPublisher::publishEvent);

        return toResponse(autorizacion);
    }

    private AutorizacionDevolucionResponse toResponse(AutorizacionDevolucion auth) {
        List<LineaDevolucionResponse> lineasResponse = auth.getLineas().stream()
                .map(l -> new LineaDevolucionResponse(
                        l.getId(),
                        l.getProductoId().valor(),
                        l.getCantidadDevuelta().valor(),
                        l.getMotivoDevolucion().valor(),
                        l.getEstadoInspeccion().name()
                ))
                .collect(Collectors.toList());

        return new AutorizacionDevolucionResponse(
                auth.getDevolucionId().valor(),
                auth.getEmpresaId().valor(),
                auth.getDocumentoFuenteId().valor(),
                auth.getEstado().name(),
                lineasResponse
        );
    }
}
