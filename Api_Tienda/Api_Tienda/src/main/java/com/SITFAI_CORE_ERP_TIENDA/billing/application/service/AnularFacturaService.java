package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.AnularFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.AnularFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class AnularFacturaService implements AnularFacturaUseCase {

    private final FacturaRepository facturaRepository;

    public AnularFacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = Objects.requireNonNull(facturaRepository);
    }

    @Override
    @Transactional
    public FacturaResponse ejecutar(AnularFacturaCommand command) {
        Objects.requireNonNull(command);

        FacturaId facturaId = new FacturaId(command.facturaId());
        UUID empresaId = command.empresaId();

        Factura factura = facturaRepository.findByIdAndEmpresaId(facturaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + command.facturaId()));

        factura.anular();
        facturaRepository.save(factura);

        return new FacturaResponse(
                factura.getId().value().toString(),
                factura.getEmpresaId().toString(),
                factura.getClienteId().value().toString(),
                factura.getPedidoId() != null ? factura.getPedidoId().value().toString() : null,
                factura.getRucCliente().valor(),
                factura.getSubtotal().monto(),
                factura.getTotalImpuestos().monto(),
                factura.getTotalGeneral().monto(),
                factura.getEstado().name(),
                java.util.Collections.emptyList()
        );
    }
}
