package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyKey;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyRecord;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.port.output.IdempotencyRepository;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.entity.IdempotencyJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.mapper.IdempotencyJpaMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdempotencyJpaAdapter implements IdempotencyRepository {

    private final SpringDataIdempotencyRepository springDataRepository;

    public IdempotencyJpaAdapter(SpringDataIdempotencyRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<IdempotencyRecord> findByEmpresaIdAndKey(EmpresaId empresaId, IdempotencyKey key) {
        return springDataRepository.findByEmpresaIdAndIdempotencyKey(empresaId.valor().toString(), key.value())
                .map(IdempotencyJpaMapper::toDomain);
    }

    @Override
    public void save(IdempotencyRecord record) {
        IdempotencyJpaEntity entity = IdempotencyJpaMapper.toEntity(record);
        springDataRepository.save(entity);
    }
}
