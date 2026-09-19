package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.IncrementarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoIngresoDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.IncrementarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class IncrementarStockService implements IncrementarStockUseCase {

    private final BodegaRepository bodegaRepository;
    private final BodegaEventPublisher eventPublisher;

    public IncrementarStockService(BodegaRepository bodegaRepository, BodegaEventPublisher eventPublisher) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository, "BodegaRepository es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "BodegaEventPublisher es obligatorio");
    }

    @Override
    @Transactional
    public void ejecutar(IncrementarStockCommand command) {
        EmpresaId empresaId = new EmpresaId(command.empresaId());
        BodegaId bodegaId = BodegaId.de(command.bodegaId());
        DocumentoFuenteId documentoFuenteId = new DocumentoFuenteId("RECEPCION", command.documentoFuenteId().toString());

        Bodega bodega = bodegaRepository.buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Bodega no encontrada o no pertenece al tenant"));

        for (MovimientoIngresoDto mov : command.movimientos()) {
            ProductoId productoId = ProductoId.de(mov.productoId());
            Cantidad cantidad = Cantidad.de(mov.cantidad());
            LoteId loteId = (mov.codigoLote() != null && !mov.codigoLote().isBlank()) 
                    ? LoteId.de(mov.codigoLote()) 
                    : null;

            bodega.registrarIngreso(productoId, cantidad, loteId, mov.fechaCaducidad(), documentoFuenteId);
        }

        bodegaRepository.guardar(bodega);

        eventPublisher.publicarTodos(bodega.drainDomainEvents());
    }
}
