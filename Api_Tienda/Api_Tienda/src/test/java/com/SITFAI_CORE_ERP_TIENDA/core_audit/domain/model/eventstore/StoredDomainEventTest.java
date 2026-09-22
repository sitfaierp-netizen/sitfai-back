package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias puras de Dominio para el Aggregate Root {@link StoredDomainEvent} (AUD-03, MT-01).
 * <p>
 * Valida la inmutabilidad, fail-fast validations, encapsulamiento y transiciones de estado
 * en estricto aislamiento de cualquier framework (Clean Architecture).
 */
@DisplayName("Auditoría de Event Store: StoredDomainEvent Domain Tests")
class StoredDomainEventTest {

    private final EmpresaId tenantId = EmpresaId.de(UUID.randomUUID());
    private final StoredEventId eventId = StoredEventId.generar();
    private final String nombreEvento = "ReservaRechazadaEvent";
    private final Instant timestampOcurrencia = Instant.parse("2026-09-22T10:15:30.00Z");
    private final String payloadJson = "{\"pedidoId\":\"123e4567-e89b-12d3-a456-426614174000\",\"motivo\":\"Stock insuficiente\"}";

    @Nested
    @DisplayName("1. Notarización e Inmutabilidad Inicial")
    class NotarizacionTests {

        @Test
        @DisplayName("Debe notariar exitosamente un evento en estado PENDIENTE con datos sellados")
        void testNotariarExitoso() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    eventId,
                    tenantId,
                    nombreEvento,
                    timestampOcurrencia,
                    payloadJson
            );

            assertNotNull(evento);
            assertEquals(eventId, evento.getId());
            assertEquals(tenantId, evento.getEmpresaId());
            assertEquals(nombreEvento, evento.getNombreEvento());
            assertEquals(timestampOcurrencia, evento.getOcurridoEn());
            assertEquals(payloadJson, evento.getPayload());
            assertEquals(EventStatus.PENDIENTE, evento.getEstado());
            assertTrue(evento.isPendiente());
            assertFalse(evento.isProcesado());
            assertFalse(evento.isFallido());
            assertNull(evento.getMotivoFallo());
            assertNull(evento.getProcesadoEn());
        }

        @Test
        @DisplayName("Debe generar ID automáticamente en la sobrecarga factory conveniente")
        void testNotariarSobrecargaConIdAutogenerado() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    tenantId,
                    nombreEvento,
                    timestampOcurrencia,
                    payloadJson
            );

            assertNotNull(evento);
            assertNotNull(evento.getId());
            assertNotNull(evento.getId().valor());
            assertEquals(tenantId, evento.getEmpresaId());
            assertEquals(EventStatus.PENDIENTE, evento.getEstado());
        }

        @Test
        @DisplayName("Debe notariar evento asignando timestamp actual cuando no se provee")
        void testNotariarSobrecargaConTimestampActual() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    tenantId,
                    nombreEvento,
                    payloadJson
            );

            assertNotNull(evento);
            assertNotNull(evento.getOcurridoEn());
            assertEquals(EventStatus.PENDIENTE, evento.getEstado());
        }
    }

    @Nested
    @DisplayName("2. Invariantes y Validaciones Fail-Fast")
    class ValidacionesFailFastTests {

        @Test
        @DisplayName("Debe lanzar excepción si StoredEventId es nulo")
        void testFailFastIdNulo() {
            NullPointerException ex = assertThrows(NullPointerException.class, () ->
                    StoredDomainEvent.notariar(null, tenantId, nombreEvento, timestampOcurrencia, payloadJson)
            );
            assertTrue(ex.getMessage().contains("StoredEventId no puede ser null"));
        }

        @Test
        @DisplayName("Debe lanzar excepción si EmpresaId es nulo (violación MT-01)")
        void testFailFastEmpresaIdNulo() {
            NullPointerException ex = assertThrows(NullPointerException.class, () ->
                    StoredDomainEvent.notariar(eventId, null, nombreEvento, timestampOcurrencia, payloadJson)
            );
            assertTrue(ex.getMessage().contains("EmpresaId no puede ser null (MT-01)"));
        }

        @Test
        @DisplayName("Debe lanzar excepción si nombreEvento es nulo o vacío")
        void testFailFastNombreEventoInvalido() {
            assertThrows(IllegalArgumentException.class, () ->
                    StoredDomainEvent.notariar(eventId, tenantId, null, timestampOcurrencia, payloadJson)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    StoredDomainEvent.notariar(eventId, tenantId, "   ", timestampOcurrencia, payloadJson)
            );
        }

        @Test
        @DisplayName("Debe lanzar excepción si ocurridoEn es nulo")
        void testFailFastOcurridoEnNulo() {
            NullPointerException ex = assertThrows(NullPointerException.class, () ->
                    StoredDomainEvent.notariar(eventId, tenantId, nombreEvento, null, payloadJson)
            );
            assertTrue(ex.getMessage().contains("ocurridoEn no puede ser null"));
        }

        @Test
        @DisplayName("Debe lanzar excepción si payload es nulo o vacío")
        void testFailFastPayloadInvalido() {
            assertThrows(IllegalArgumentException.class, () ->
                    StoredDomainEvent.notariar(eventId, tenantId, nombreEvento, timestampOcurrencia, null)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    StoredDomainEvent.notariar(eventId, tenantId, nombreEvento, timestampOcurrencia, " \t\n ")
            );
        }
    }

    @Nested
    @DisplayName("3. Máquina de Estados y Transiciones del Agregado")
    class TransicionesEstadoTests {

        @Test
        @DisplayName("Debe transicionar a PROCESADO sellando fecha y limpiando motivos de fallo")
        void testMarcarProcesado() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    eventId, tenantId, nombreEvento, timestampOcurrencia, payloadJson
            );

            Instant fechaProcesado = Instant.parse("2026-09-22T10:16:00.00Z");
            evento.marcarProcesado(fechaProcesado);

            assertEquals(EventStatus.PROCESADO, evento.getEstado());
            assertTrue(evento.isProcesado());
            assertFalse(evento.isPendiente());
            assertFalse(evento.isFallido());
            assertEquals(fechaProcesado, evento.getProcesadoEn());
            assertNull(evento.getMotivoFallo());
        }

        @Test
        @DisplayName("Debe transicionar a FALLIDO registrando el motivo y fecha")
        void testMarcarFallido() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    eventId, tenantId, nombreEvento, timestampOcurrencia, payloadJson
            );

            Instant fechaFallo = Instant.parse("2026-09-22T10:17:00.00Z");
            evento.marcarFallido("Timeout de conexión al despachar hacia broker", fechaFallo);

            assertEquals(EventStatus.FALLIDO, evento.getEstado());
            assertTrue(evento.isFallido());
            assertFalse(evento.isProcesado());
            assertFalse(evento.isPendiente());
            assertEquals("Timeout de conexión al despachar hacia broker", evento.getMotivoFallo());
            assertEquals(fechaFallo, evento.getProcesadoEn());
        }

        @Test
        @DisplayName("Prohibido marcar como FALLIDO un evento que ya fue sellado como PROCESADO")
        void testProhibidoFallarEventoYaProcesado() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    eventId, tenantId, nombreEvento, timestampOcurrencia, payloadJson
            );
            evento.marcarProcesado();

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    evento.marcarFallido("Fallo espurio reportado tardíamente")
            );
            assertTrue(ex.getMessage().contains("No se puede marcar como FALLIDO un evento que ya fue PROCESADO"));
        }

        @Test
        @DisplayName("Debe rechazar motivo de fallo nulo o vacío")
        void testMotivoFalloInvalido() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    eventId, tenantId, nombreEvento, timestampOcurrencia, payloadJson
            );

            assertThrows(IllegalArgumentException.class, () -> evento.marcarFallido(null));
            assertThrows(IllegalArgumentException.class, () -> evento.marcarFallido("   "));
        }

        @Test
        @DisplayName("Permite reintentar un evento en estado FALLIDO y transicionarlo a PROCESADO")
        void testRecuperacionDesdeFallidoAProcesado() {
            StoredDomainEvent evento = StoredDomainEvent.notariar(
                    eventId, tenantId, nombreEvento, timestampOcurrencia, payloadJson
            );
            evento.marcarFallido("Falla transitoria de red");
            assertTrue(evento.isFallido());

            evento.marcarProcesado();
            assertTrue(evento.isProcesado());
            assertNull(evento.getMotivoFallo(), "El motivo de fallo debe limpiarse al recuperarse exitosamente.");
        }
    }

    @Nested
    @DisplayName("4. Reconstitución desde Persistencia")
    class ReconstitucionTests {

        @Test
        @DisplayName("Debe reconstituir fielmente el estado histórico completo")
        void testReconstituirFielmente() {
            Instant fechaProcesado = Instant.parse("2026-09-22T10:20:00.00Z");
            StoredDomainEvent reconstituido = StoredDomainEvent.reconstituir(
                    eventId,
                    tenantId,
                    nombreEvento,
                    timestampOcurrencia,
                    payloadJson,
                    EventStatus.FALLIDO,
                    "Error de deserialización previo",
                    fechaProcesado
            );

            assertEquals(eventId, reconstituido.getId());
            assertEquals(tenantId, reconstituido.getEmpresaId());
            assertEquals(nombreEvento, reconstituido.getNombreEvento());
            assertEquals(timestampOcurrencia, reconstituido.getOcurridoEn());
            assertEquals(payloadJson, reconstituido.getPayload());
            assertEquals(EventStatus.FALLIDO, reconstituido.getEstado());
            assertEquals("Error de deserialización previo", reconstituido.getMotivoFallo());
            assertEquals(fechaProcesado, reconstituido.getProcesadoEn());
        }
    }

    @Nested
    @DisplayName("5. Value Objects de Event Store")
    class ValueObjectsTests {

        @Test
        @DisplayName("StoredEventId: inmutabilidad, parseo y validaciones")
        void testStoredEventId() {
            UUID rawUuid = UUID.randomUUID();
            StoredEventId vo1 = StoredEventId.de(rawUuid);
            StoredEventId vo2 = StoredEventId.de(rawUuid.toString());

            assertEquals(vo1, vo2);
            assertEquals(rawUuid.toString(), vo1.toString());
            assertThrows(NullPointerException.class, () -> new StoredEventId(null));
            assertThrows(IllegalArgumentException.class, () -> StoredEventId.de(""));
            assertThrows(IllegalArgumentException.class, () -> StoredEventId.de("invalid-uuid"));
        }

        @Test
        @DisplayName("EmpresaId: inmutabilidad, parseo y validaciones MT-01")
        void testEmpresaId() {
            UUID rawUuid = UUID.randomUUID();
            EmpresaId vo1 = EmpresaId.de(rawUuid);
            EmpresaId vo2 = EmpresaId.de(rawUuid.toString());

            assertEquals(vo1, vo2);
            assertEquals(rawUuid.toString(), vo1.toString());
            assertThrows(NullPointerException.class, () -> new EmpresaId(null));
            assertThrows(IllegalArgumentException.class, () -> EmpresaId.de(""));
            assertThrows(IllegalArgumentException.class, () -> EmpresaId.de("invalid-uuid"));
        }

        @Test
        @DisplayName("StoredDomainEvent: igualdad basada en ID")
        void testStoredDomainEventEquality() {
            StoredDomainEvent evento1 = StoredDomainEvent.notariar(
                    eventId, tenantId, nombreEvento, timestampOcurrencia, payloadJson
            );
            StoredDomainEvent evento2 = StoredDomainEvent.notariar(
                    eventId, tenantId, "OtroEvento", Instant.now(), "{}"
            );

            assertEquals(evento1, evento2);
            assertEquals(evento1.hashCode(), evento2.hashCode());
        }
    }
}
