package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper.FacturaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.ConsultarFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.FacturaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * APPLICATION SERVICE: Consultar Factura (MT-01).
 */
@Service
public class ConsultarFacturaService implements ConsultarFacturaUseCase {

    private final FacturaRepository facturaRepository;

    public ConsultarFacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = Objects.requireNonNull(facturaRepository, "facturaRepository es obligatorio.");
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse consultarPorId(FacturaId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "FacturaId no puede ser null.");
        Objects.requireNonNull(empresaId, "EmpresaId no puede ser null.");

        FacturaElectronica factura = facturaRepository.buscarPorId(id, empresaId)
                .orElseThrow(() -> new FacturaNoEncontradaException(id, empresaId));

        return FacturaApplicationMapper.aResponse(factura);
    }
}
