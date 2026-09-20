package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ReingresarStockPorDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ReingresarStockPorDevolucionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReingresarStockPorDevolucionService implements ReingresarStockPorDevolucionUseCase {

    private final BodegaRepository bodegaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReingresarStockPorDevolucionService(BodegaRepository bodegaRepository, ApplicationEventPublisher eventPublisher) {
        this.bodegaRepository = bodegaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void ejecutar(ReingresarStockPorDevolucionCommand command) {
        // En un flujo asíncrono el MT-01 se garantiza inyectando la empresaId extraída del Payload directamente en el comando.
        EmpresaId empresaId = new EmpresaId(command.empresaId());
        
        // Buscamos la bodega principal o la asociada a la sucursal del POS.
        List<Bodega> bodegas = bodegaRepository.listarPorSucursal(empresaId, command.sucursalId().toString());
        Bodega bodega = bodegas.stream()
                .filter(Bodega::puedeVender)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe una bodega activa para ventas en la sucursal " + command.sucursalId()));

        DocumentoFuenteId documentoFuente = new DocumentoFuenteId("DEVOLUCION_POS", command.ventaOrigenId().toString());

        for (var loteDto : command.lotesRevertidos()) {
            ProductoId productoId = new ProductoId(loteDto.productoId());
            Cantidad cantidad = Cantidad.de(loteDto.cantidad());
            LoteId loteId = LoteId.de(loteDto.codigoLote());
            
            bodega.reingresarStock(productoId, cantidad, loteId, documentoFuente);
        }

        Bodega bodegaGuardada = bodegaRepository.guardar(bodega);

        // AUD-03: Drenar y publicar los Domain Events
        bodegaGuardada.drainDomainEvents().forEach(eventPublisher::publishEvent);
    }
}
