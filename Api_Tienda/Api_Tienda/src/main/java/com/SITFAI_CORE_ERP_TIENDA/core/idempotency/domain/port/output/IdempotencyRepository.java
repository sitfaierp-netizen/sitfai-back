package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyKey;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyRepository {
    Optional<IdempotencyRecord> findByEmpresaIdAndKey(EmpresaId empresaId, IdempotencyKey key);
    void save(IdempotencyRecord record);
}
