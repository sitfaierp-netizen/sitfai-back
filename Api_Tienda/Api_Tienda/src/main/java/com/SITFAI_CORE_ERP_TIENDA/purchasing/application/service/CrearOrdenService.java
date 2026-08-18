package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper.OrdenCompraApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProveedorId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CrearOrdenService implements CrearOrdenUseCase {

    private final OrdenCompraRepository repository;

    public CrearOrdenService(OrdenCompraRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OrdenCompraResponse crearBorrador(CrearBorradorCommand command) {
        OrdenCompra orden = OrdenCompra.crearBorrador(
                new OrdenCompraId(UUID.randomUUID()),
                new EmpresaId(command.empresaId()),
                new ProveedorId(command.proveedorId())
        );

        repository.guardar(orden);
        return OrdenCompraApplicationMapper.aResponse(orden);
    }
}
