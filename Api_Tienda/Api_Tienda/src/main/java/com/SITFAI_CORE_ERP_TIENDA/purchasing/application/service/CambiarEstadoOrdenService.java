package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CambiarEstadoOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CambiarEstadoOrdenService implements CambiarEstadoOrdenUseCase {

    private final OrdenCompraRepository repository;
    private final OrdenCompraEventPublisher eventPublisher;

    public CambiarEstadoOrdenService(OrdenCompraRepository repository, OrdenCompraEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrdenCompraResponse cambiarEstado(CambiarEstadoCommand command) {
        OrdenCompra orden = repository.buscarPorId(
                new OrdenCompraId(command.ordenCompraId()),
                new EmpresaId(command.empresaId())
        ).orElseThrow(() -> new DomainException("Orden de Compra no encontrada."));

        switch (command.nuevoEstado().toUpperCase()) {
            case "EMITIDA" -> orden.emitir();
            case "CANCELADA" -> orden.cancelar();
            case "RECIBIDA" -> orden.marcarComoRecibida();
            default -> throw new IllegalArgumentException("Estado no soportado: " + command.nuevoEstado());
        }

        repository.guardar(orden);

        // Publicar Domain Events (OrdenCompraRecibidaEvent para el Putaway)
        orden.getDomainEvents().forEach(eventPublisher::publicar);

        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
