package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.port.output.OrdenCompraRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Shared application operation for purchase-order mutations.
 *
 * <p>Tenant trust is deliberately resolved by the calling input use case. This operation
 * knows neither HTTP/JWT security nor messaging infrastructure.</p>
 */
@Component
public class OrdenCompraApplicationOperation {

    private final OrdenCompraRepository repository;

    public OrdenCompraApplicationOperation(OrdenCompraRepository repository) {
        this.repository = repository;
    }

    public OrdenCompraResponse crearBorrador(UUID empresaId, UUID proveedorId, String actorId) {
        OrdenCompra orden = OrdenCompra.crear(
                OrdenCompraId.generar(),
                empresaId,
                new ProveedorId(proveedorId),
                actorId
        );

        repository.guardar(orden);
        return OrdenCompraApplicationMapper.aResponse(orden);
    }

    public OrdenCompraResponse agregarLinea(
            UUID ordenCompraId,
            UUID empresaId,
            UUID productoId,
            BigDecimal cantidad,
            BigDecimal costoUnitario,
            String actorId) {
        OrdenCompra orden = repository.buscarPorIdYEmpresaId(
                new OrdenCompraId(ordenCompraId),
                empresaId
        ).orElseThrow(() -> new DomainException("Orden de Compra no encontrada o no pertenece al tenant."));

        orden.setUpdatedBy(actorId);
        orden.agregarLinea(new LineaOrdenCompra(
                UUID.randomUUID(),
                new ProductoId(productoId),
                cantidad.intValue(),
                new Dinero(costoUnitario)
        ));

        repository.guardar(orden);
        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
