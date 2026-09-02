package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.FacturaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.mapper.FacturaPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository.SpringDataFacturaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FacturaJpaAdapter implements FacturaRepository {

    private final SpringDataFacturaRepository repository;

    public FacturaJpaAdapter(SpringDataFacturaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Factura factura) {
        FacturaJpaEntity entity = FacturaPersistenceMapper.toEntity(factura);
        repository.save(entity);
    }

    @Override
    public Optional<Factura> findByIdAndEmpresaId(FacturaId id, UUID empresaId) {
        return repository.findByIdAndEmpresaId(id.value(), empresaId)
                .map(FacturaPersistenceMapper::toDomain);
    }
}
