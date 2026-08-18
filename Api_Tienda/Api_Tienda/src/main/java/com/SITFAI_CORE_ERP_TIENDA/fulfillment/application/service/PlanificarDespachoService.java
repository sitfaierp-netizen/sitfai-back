package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.PlanificarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.mapper.OrdenDespachoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.PlanificarDespachoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.OrdenDespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.LineaDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DireccionEntrega;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.PedidoOrigenId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PlanificarDespachoService implements PlanificarDespachoUseCase {

    private final OrdenDespachoRepository repository;

    public PlanificarDespachoService(OrdenDespachoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    @Transactional
    public OrdenDespachoResponse ejecutar(PlanificarDespachoCommand command) {
        Objects.requireNonNull(command);

        EmpresaId empresaId = new EmpresaId(command.empresaId());
        PedidoOrigenId pedidoId = new PedidoOrigenId(command.pedidoOrigenId());
        DireccionEntrega direccion = new DireccionEntrega(command.direccionLocal(), command.ciudad(), command.codigoPostal());

        List<LineaDespacho> lineas = command.lineas().stream()
                .map(l -> LineaDespacho.crear(new ProductoId(l.productoId()), Cantidad.de(l.cantidadSolicitada())))
                .collect(Collectors.toList());

        OrdenDespacho orden = OrdenDespacho.planificar(empresaId, pedidoId, direccion, lineas);
        OrdenDespacho ordenGuardada = repository.guardar(orden);

        return OrdenDespachoApplicationMapper.toResponse(ordenGuardada);
    }
}
