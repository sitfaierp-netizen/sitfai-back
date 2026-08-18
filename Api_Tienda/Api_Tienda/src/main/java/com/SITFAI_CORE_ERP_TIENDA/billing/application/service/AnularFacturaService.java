package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.AnularFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper.FacturaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.AnularFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.FacturaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * APPLICATION SERVICE: Anular Factura.
 * Orquesta la anulación de una FacturaElectronica respetando MT-01.
 */
@Service
public class AnularFacturaService implements AnularFacturaUseCase {

    private final FacturaRepository facturaRepository;
    private final FacturaEventPublisher eventPublisher;

    public AnularFacturaService(
            FacturaRepository facturaRepository,
            FacturaEventPublisher eventPublisher) {
        this.facturaRepository = Objects.requireNonNull(facturaRepository, "facturaRepository es obligatorio.");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher es obligatorio.");
    }

    @Override
    @Transactional
    public FacturaResponse ejecutar(AnularFacturaCommand command) {
        Objects.requireNonNull(command, "AnularFacturaCommand no puede ser null.");

        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        FacturaId facturaId = FacturaId.de(command.facturaId());

        // 1. Recuperación con aislamiento de Tenant (MT-01)
        FacturaElectronica factura = facturaRepository.buscarPorId(facturaId, empresaId)
                .orElseThrow(() -> new FacturaNoEncontradaException(facturaId, empresaId));

        // 2. Delegar lógica de transición al Dominio (REGLA-1)
        factura.anular(command.motivo());

        // 3. Persistir
        facturaRepository.guardar(factura);

        // 4. Publicar Domain Events
        factura.getDomainEvents().forEach(eventPublisher::publicar);

        // 5. Retornar DTO
        return FacturaApplicationMapper.aResponse(factura);
    }
}
