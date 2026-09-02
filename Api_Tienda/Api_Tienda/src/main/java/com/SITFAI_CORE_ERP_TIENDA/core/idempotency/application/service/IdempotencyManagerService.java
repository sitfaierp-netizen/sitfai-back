package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyKey;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyRecord;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.PayloadFingerprint;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.port.output.IdempotencyRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Servicio de Aplicación para orquestar la idempotencia de forma independiente a HTTP.
 */
public class IdempotencyManagerService {

    private final IdempotencyRepository repository;
    private final long ttlMillis;

    public IdempotencyManagerService(IdempotencyRepository repository, long ttlMillis) {
        this.repository = repository;
        this.ttlMillis = ttlMillis;
    }

    /**
     * Procesa la llave antes de ejecutar la lógica de negocio.
     * Retorna el registro de idempotencia.
     */
    public IdempotencyRecord processRequest(EmpresaId empresaId, IdempotencyKey key, PayloadFingerprint fingerprint) {
        Optional<IdempotencyRecord> existingOpt = repository.findByEmpresaIdAndKey(empresaId, key);

        if (existingOpt.isPresent()) {
            IdempotencyRecord record = existingOpt.get();
            record.checkExpiration(Instant.now(), ttlMillis);
            record.checkAndLockForProcessing(fingerprint);
            if (!record.isCompleted()) {
                repository.save(record);
            }
            return record;
        }

        IdempotencyRecord newRecord = IdempotencyRecord.beginProcessing(empresaId, key, fingerprint);
        repository.save(newRecord);
        return newRecord;
    }

    /**
     * Marca la petición como completada con éxito.
     */
    public void completeRequest(EmpresaId empresaId, IdempotencyKey key, Integer status, String responseBody) {
        repository.findByEmpresaIdAndKey(empresaId, key).ifPresent(record -> {
            record.markAsCompleted(status, responseBody);
            repository.save(record);
        });
    }

    /**
     * Marca la petición como fallida (para permitir reintentos).
     */
    public void failRequest(EmpresaId empresaId, IdempotencyKey key, Integer status, String responseBody) {
        repository.findByEmpresaIdAndKey(empresaId, key).ifPresent(record -> {
            record.markAsFailed(status, responseBody);
            repository.save(record);
        });
    }
}
