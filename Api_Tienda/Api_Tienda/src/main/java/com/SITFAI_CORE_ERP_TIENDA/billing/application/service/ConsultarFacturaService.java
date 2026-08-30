package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper.FacturaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.ConsultarFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class ConsultarFacturaService implements ConsultarFacturaUseCase {

    private final FacturaRepository facturaRepository;

    public ConsultarFacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = Objects.requireNonNull(facturaRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse consultarPorId(FacturaId id, UUID empresaId) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(empresaId);

        Factura factura = facturaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + id.value()));

        return FacturaApplicationMapper.toResponse(factura);
    }
}
