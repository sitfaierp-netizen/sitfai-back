package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.RegistrarPickingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.mapper.OrdenDespachoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.RegistrarPickingUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.OrdenDespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.exception.DespachoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class RegistrarPickingService implements RegistrarPickingUseCase {

    private final OrdenDespachoRepository repository;

    public RegistrarPickingService(OrdenDespachoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    @Transactional
    public OrdenDespachoResponse ejecutar(RegistrarPickingCommand command) {
        Objects.requireNonNull(command);

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        DespachoId despachoId = new DespachoId(command.despachoId());
        ProductoId productoId = new ProductoId(command.productoId());
        Cantidad cantidad = Cantidad.de(command.cantidadPreparada());

        OrdenDespacho orden = repository.buscarPorId(despachoId, empresaId)
                .orElseThrow(() -> new DespachoNoEncontradoException(despachoId, empresaId));

        orden.registrarPicking(productoId, cantidad);

        OrdenDespacho ordenActualizada = repository.guardar(orden);

        return OrdenDespachoApplicationMapper.toResponse(ordenActualizada);
    }
}
