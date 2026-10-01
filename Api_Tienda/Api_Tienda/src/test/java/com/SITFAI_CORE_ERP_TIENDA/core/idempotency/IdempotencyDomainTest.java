package com.SITFAI_CORE_ERP_TIENDA.core.idempotency;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.exception.ConcurrentProcessingException;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.exception.IdempotencyConflictException;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyKey;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyRecord;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.IdempotencyStatus;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.PayloadFingerprint;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.EmpresaId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyDomainTest {

    private final EmpresaId empresaId = EmpresaId.generar();
    private final IdempotencyKey key = new IdempotencyKey(UUID.randomUUID().toString());
    private final PayloadFingerprint hash1 = new PayloadFingerprint("HASH123");
    private final PayloadFingerprint hash2 = new PayloadFingerprint("HASH456");

    @Test
    void test1_primeraSolicitudExitosa() {
        IdempotencyRecord record = IdempotencyRecord.beginProcessing(empresaId, key, hash1);
        assertEquals(IdempotencyStatus.PROCESSING, record.getStatus());
        assertFalse(record.isCompleted());
    }

    @Test
    void test3_mismaKeyPayloadDiferente_Conflict() {
        IdempotencyRecord record = IdempotencyRecord.beginProcessing(empresaId, key, hash1);
        assertThrows(IdempotencyConflictException.class, () -> record.checkAndLockForProcessing(hash2));
    }

    @Test
    void test4_keysDiferentesSeProcesanPorSeparado() {
        IdempotencyKey key2 = new IdempotencyKey(UUID.randomUUID().toString());
        IdempotencyRecord r1 = IdempotencyRecord.beginProcessing(empresaId, key, hash1);
        IdempotencyRecord r2 = IdempotencyRecord.beginProcessing(empresaId, key2, hash2);
        assertNotEquals(r1.getIdempotencyKey(), r2.getIdempotencyKey());
    }

    @Test
    void test5_aislamientoEntreTenants() {
        EmpresaId empresa2 = EmpresaId.generar();
        IdempotencyRecord r1 = IdempotencyRecord.beginProcessing(empresaId, key, hash1);
        IdempotencyRecord r2 = IdempotencyRecord.beginProcessing(empresa2, key, hash1);
        assertNotEquals(r1.getEmpresaId(), r2.getEmpresaId());
    }

    @Test
    void test7_operacionEnEstadoProcessing_rechazaNuevosIntentos() {
        IdempotencyRecord record = IdempotencyRecord.beginProcessing(empresaId, key, hash1);
        assertThrows(ConcurrentProcessingException.class, () -> record.checkAndLockForProcessing(hash1));
    }

    @Test
    void test8_operacionEnEstadoCompleted_noLanzaExceptionPeroMantieneEstado() {
        IdempotencyRecord record = IdempotencyRecord.beginProcessing(empresaId, key, hash1);
        record.markAsCompleted(200, "OK");
        assertDoesNotThrow(() -> record.checkAndLockForProcessing(hash1));
        assertTrue(record.isCompleted());
    }

    @Test
    void test9_operacionEnEstadoFailed_permiteReintentoSeguro() {
        IdempotencyRecord record = IdempotencyRecord.beginProcessing(empresaId, key, hash1);
        record.markAsFailed(500, "ERROR");
        assertEquals(IdempotencyStatus.FAILED, record.getStatus());

        record.checkAndLockForProcessing(hash1); // Reintenta
        assertEquals(IdempotencyStatus.PROCESSING, record.getStatus());
    }

    @Test
    void test10_expiracionDeEstado() {
        IdempotencyRecord record = IdempotencyRecord.reconstitute(
                UUID.randomUUID(), empresaId, key, hash1, IdempotencyStatus.PROCESSING,
                null, null, Instant.now().minusSeconds(1000)
        );

        // TTL = 500 seconds
        record.checkExpiration(Instant.now(), 500000);
        assertEquals(IdempotencyStatus.EXPIRED, record.getStatus());
        
        // Debe permitir reproceso tras expirado
        record.checkAndLockForProcessing(hash1);
        assertEquals(IdempotencyStatus.PROCESSING, record.getStatus());
    }
}
