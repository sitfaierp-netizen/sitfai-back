package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.ListarOrdenesCompraUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListarOrdenesCompraService implements ListarOrdenesCompraUseCase {

    private final OrdenCompraRepository repository;

    public ListarOrdenesCompraService(OrdenCompraRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<OrdenCompraResponse> listarOrdenes(UUID empresaId, Pageable pageable) {
        return repository.listarOrdenes(empresaId, pageable)
                .map(OrdenCompraApplicationMapper::aResponse);
    }
}
