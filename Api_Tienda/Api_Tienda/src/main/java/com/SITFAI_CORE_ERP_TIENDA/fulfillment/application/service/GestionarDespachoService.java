package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.ConfirmarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.EmpacarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.IniciarPickingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.LineaDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.GestionarDespachoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class GestionarDespachoService implements GestionarDespachoUseCase {

    private final com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.port.output.OrdenDespachoRepository repository;
    private final TenantProviderPort tenantProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public GestionarDespachoService(
            com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.port.output.OrdenDespachoRepository repository,
            TenantProviderPort tenantProviderPort,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.tenantProviderPort = tenantProviderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrdenDespachoResponse iniciarPicking(IniciarPickingCommand command) {
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        DespachoId despachoId = DespachoId.de(command.despachoId());
        
        OrdenDespacho orden = repository.buscarPorId(empresaId, despachoId)
                .orElseThrow(() -> new IllegalArgumentException("Despacho no encontrado"));
                
        orden.iniciarPicking();
        repository.guardar(empresaId, orden);
        
        return toResponse(orden);
    }

    @Override
    @Transactional
    public OrdenDespachoResponse empacar(EmpacarDespachoCommand command) {
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        DespachoId despachoId = DespachoId.de(command.despachoId());
        
        OrdenDespacho orden = repository.buscarPorId(empresaId, despachoId)
                .orElseThrow(() -> new IllegalArgumentException("Despacho no encontrado"));
                
        orden.completarEmpaque();
        repository.guardar(empresaId, orden);
        
        return toResponse(orden);
    }

    @Override
    @Transactional
    public OrdenDespachoResponse confirmar(ConfirmarDespachoCommand command) {
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        DespachoId despachoId = DespachoId.de(command.despachoId());
        
        OrdenDespacho orden = repository.buscarPorId(empresaId, despachoId)
                .orElseThrow(() -> new IllegalArgumentException("Despacho no encontrado"));
                
        orden.confirmarDespacho();
        repository.guardar(empresaId, orden);
        
        orden.pullDomainEvents().forEach(eventPublisher::publishEvent);
        
        return toResponse(orden);
    }

    private OrdenDespachoResponse toResponse(OrdenDespacho orden) {
        var lineas = orden.getLineas().stream()
                .map(l -> new LineaDespachoResponse(l.getId(), l.getProductoId().valor(), l.getCantidad()))
                .collect(Collectors.toList());
                
        return new OrdenDespachoResponse(
                orden.getDespachoId().valor(),
                orden.getEmpresaId().valor(),
                orden.getPedidoId().valor(),
                orden.getBodegaId().valor(),
                orden.getEstado().name(),
                lineas
        );
    }
}
