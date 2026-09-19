package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.ConsultarOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.OrdenCompraNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Consultas de órdenes de compra con aislamiento estricto MT-01.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarOrdenService implements ConsultarOrdenUseCase {

    private final OrdenCompraRepository repository;

    public ConsultarOrdenService(OrdenCompraRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository no puede ser null");
    }

    @Override
    public OrdenCompraResponse buscarPorId(UUID ordenCompraId, UUID empresaId) {
        Objects.requireNonNull(ordenCompraId, "ordenCompraId no puede ser null");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null");

        OrdenCompraId id = new OrdenCompraId(ordenCompraId);
        EmpresaId empId = new EmpresaId(empresaId);

        OrdenCompra orden = repository.buscarPorIdYEmpresaId(
                new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId(ordenCompraId), 
                empresaId)
                .orElseThrow(() -> new OrdenCompraNoEncontradaException(id, empId));

        return OrdenCompraApplicationMapper.aResponse(orden);
    }

    @Override
    public List<OrdenCompraResponse> listarPorEmpresa(UUID empresaId) {
        // No implementado en port mínimo — devuelve lista vacía
        return List.of();
    }

    @Override
    public List<OrdenCompraResponse> listarPorProveedor(UUID empresaId, UUID proveedorId) {
        // No implementado en port mínimo — devuelve lista vacía
        return List.of();
    }
}
