package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ReservarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ReservarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ReservarStockService implements ReservarStockUseCase {

    private final BodegaRepository bodegaRepository;
    private final BodegaEventPublisher eventPublisher;

    public ReservarStockService(BodegaRepository bodegaRepository, BodegaEventPublisher eventPublisher) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void ejecutar(ReservarStockCommand command) {
        EmpresaId empresaId = command.toEmpresaId();

        // Buscamos una bodega de venta para el tenant dado
        List<Bodega> bodegas = bodegaRepository.listarActivasPorEmpresa(empresaId);
        Optional<Bodega> bodegaVentaOpt = bodegas.stream()
                .filter(Bodega::puedeVender)
                .findFirst();

        if (bodegaVentaOpt.isEmpty()) {
            throw new BodegaNoEncontradaException(null, empresaId);
        }

        Bodega bodega = bodegaVentaOpt.get();

        bodega.reservarStock(command.toProductoId(), command.toCantidad(), command.toDocumentoFuenteId());

        bodegaRepository.guardar(bodega);

        eventPublisher.publicarTodos(bodega.drainDomainEvents());
    }
}
