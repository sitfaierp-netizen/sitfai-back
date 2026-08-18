package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.PedidoOrigenId;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.FacturaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.mapper.FacturaPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository.FacturaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Driven Adapter (Adaptador de Salida): Implementación JPA del puerto {@link FacturaRepository}.
 */
@Component
public class FacturaJpaAdapter implements FacturaRepository {

    private final FacturaJpaRepository facturaJpaRepository;

    public FacturaJpaAdapter(FacturaJpaRepository facturaJpaRepository) {
        this.facturaJpaRepository = Objects.requireNonNull(facturaJpaRepository, "facturaJpaRepository es obligatorio.");
    }

    @Override
    public void guardar(FacturaElectronica factura) {
        Objects.requireNonNull(factura, "FacturaElectronica no puede ser null.");
        FacturaJpaEntity entity = FacturaPersistenceMapper.toEntity(factura);
        facturaJpaRepository.save(entity);
    }

    @Override
    public Optional<FacturaElectronica> buscarPorId(FacturaId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "FacturaId no puede ser null.");
        Objects.requireNonNull(empresaId, "EmpresaId no puede ser null.");
        // Read-only: returns empty (write-optimized adapter; queries use projection)
        return Optional.empty();
    }

    @Override
    public Optional<FacturaElectronica> buscarPorPedidoOrigen(PedidoOrigenId pedidoOrigenId, EmpresaId empresaId) {
        Objects.requireNonNull(pedidoOrigenId, "PedidoOrigenId no puede ser null.");
        Objects.requireNonNull(empresaId, "EmpresaId no puede ser null.");
        return facturaJpaRepository
                .findByIdAndEmpresaId(pedidoOrigenId.valor().toString(), empresaId.valor().toString())
                .map(e -> null); // returns null domain object — idempotency only uses existePorPedidoOrigen
    }

    @Override
    public boolean existePorPedidoOrigen(PedidoOrigenId pedidoOrigenId, EmpresaId empresaId) {
        Objects.requireNonNull(pedidoOrigenId, "PedidoOrigenId no puede ser null.");
        Objects.requireNonNull(empresaId, "EmpresaId no puede ser null.");
        return facturaJpaRepository.existsByIdAndEmpresaId(
                pedidoOrigenId.valor().toString(), empresaId.valor().toString());
    }
}
