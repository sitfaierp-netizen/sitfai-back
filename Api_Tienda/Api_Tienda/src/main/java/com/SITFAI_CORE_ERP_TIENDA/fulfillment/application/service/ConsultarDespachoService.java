package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.mapper.OrdenDespachoApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.ConsultarDespachoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.OrdenDespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.exception.DespachoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class ConsultarDespachoService implements ConsultarDespachoUseCase {

    private final OrdenDespachoRepository repository;

    public ConsultarDespachoService(OrdenDespachoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public OrdenDespachoResponse porId(UUID id, UUID empresaId) {
        DespachoId despachoId = new DespachoId(id);
        EmpresaId eId = new EmpresaId(empresaId);

        OrdenDespacho orden = repository.buscarPorId(despachoId, eId)
                .orElseThrow(() -> new DespachoNoEncontradoException(despachoId, eId));

        return OrdenDespachoApplicationMapper.toResponse(orden);
    }
}
