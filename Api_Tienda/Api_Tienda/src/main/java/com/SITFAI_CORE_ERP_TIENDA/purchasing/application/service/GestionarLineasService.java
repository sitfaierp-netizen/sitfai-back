package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.GestionarLineasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.LineaOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionarLineasService implements GestionarLineasUseCase {

    private final OrdenCompraRepository repository;

    public GestionarLineasService(OrdenCompraRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OrdenCompraResponse agregarLinea(AgregarLineaCommand command) {
        OrdenCompra orden = repository.buscarPorId(
                new OrdenCompraId(command.ordenCompraId()),
                new EmpresaId(command.empresaId())
        ).orElseThrow(() -> new DomainException("Orden de Compra no encontrada o no pertenece al tenant."));

        LineaOrdenCompra linea = new LineaOrdenCompra(
                new ProductoId(command.productoId()),
                command.cantidad(),
                new Dinero(command.costoUnitario(), "COP") // Moneda base
        );

        orden.agregarLinea(linea);
        repository.guardar(orden);
        
        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
