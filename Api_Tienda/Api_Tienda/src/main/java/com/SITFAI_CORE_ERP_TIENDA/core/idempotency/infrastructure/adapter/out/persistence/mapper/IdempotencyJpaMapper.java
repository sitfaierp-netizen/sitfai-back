package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyKey;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyRecord;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyStatus;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.PayloadFingerprint;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.entity.IdempotencyJpaEntity;

public class IdempotencyJpaMapper {

    public static IdempotencyJpaEntity toEntity(IdempotencyRecord domain) {
        if (domain == null) return null;
        return new IdempotencyJpaEntity(
                domain.getId(),
                domain.getEmpresaId().valor().toString(),
                domain.getIdempotencyKey().value(),
                domain.getRequestHash().value(),
                domain.getStatus().name(),
                domain.getHttpStatus(),
                domain.getResponseBody(),
                domain.getCreatedAt()
        );
    }

    public static IdempotencyRecord toDomain(IdempotencyJpaEntity entity) {
        if (entity == null) return null;
        return IdempotencyRecord.reconstitute(
                entity.getId(),
                EmpresaId.de(entity.getEmpresaId()),
                new IdempotencyKey(entity.getIdempotencyKey()),
                new PayloadFingerprint(entity.getRequestHash()),
                IdempotencyStatus.valueOf(entity.getStatus()),
                entity.getHttpStatus(),
                entity.getResponseBody(),
                entity.getCreatedAt()
        );
    }
}
