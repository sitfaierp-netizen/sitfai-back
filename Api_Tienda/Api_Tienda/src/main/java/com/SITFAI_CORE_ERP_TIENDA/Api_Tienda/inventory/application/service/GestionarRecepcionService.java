package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CompletarRecepcionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarProductoRecibidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.GestionarRecepcionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.RecepcionMercancia;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.CantidadRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.RecepcionMercanciaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class GestionarRecepcionService implements GestionarRecepcionUseCase {

    private final RecepcionMercanciaRepository repository;
    private final TenantProviderPort tenantProvider;
    private final ApplicationEventPublisher eventPublisher;

    public GestionarRecepcionService(RecepcionMercanciaRepository repository, TenantProviderPort tenantProvider, ApplicationEventPublisher eventPublisher) {
        this.repository = Objects.requireNonNull(repository);
        this.tenantProvider = Objects.requireNonNull(tenantProvider);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void registrarProductoRecibido(RegistrarProductoRecibidoCommand command) {
        EmpresaId empresaId = new EmpresaId(tenantProvider.getEmpresaIdAutenticada().valor());
        RecepcionId recepcionId = new RecepcionId(command.recepcionId());
        ProductoId productoId = new ProductoId(command.productoId());
        CantidadRecepcion cantidad = new CantidadRecepcion(command.cantidadRecepcion());

        RecepcionMercancia recepcion = repository.buscarPorId(recepcionId, empresaId)
                .orElseThrow(() -> new IllegalStateException("Recepción no encontrada"));

        recepcion.recibirProducto(productoId, cantidad);

        repository.guardar(recepcion, empresaId);
    }

    @Override
    public void completarRecepcion(CompletarRecepcionCommand command) {
        EmpresaId empresaId = new EmpresaId(tenantProvider.getEmpresaIdAutenticada().valor());
        RecepcionId recepcionId = new RecepcionId(command.recepcionId());

        RecepcionMercancia recepcion = repository.buscarPorId(recepcionId, empresaId)
                .orElseThrow(() -> new IllegalStateException("Recepción no encontrada"));

        recepcion.completar();

        repository.guardar(recepcion, empresaId);

        // Publicar eventos de dominio
        recepcion.obtenerEventosDominio().forEach(eventPublisher::publishEvent);
    }
}
