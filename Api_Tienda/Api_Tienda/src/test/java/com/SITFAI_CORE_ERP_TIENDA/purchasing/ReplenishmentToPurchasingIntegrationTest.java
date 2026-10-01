package com.SITFAI_CORE_ERP_TIENDA.purchasing;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.SolicitudAbastecimientoRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.SolicitudAbastecimiento;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EstadoSolicitud;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.event.NecesidadAbastecimientoDetectadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prueba de Integración: Orquestación Asíncrona Replenishment → Purchasing.
 * <p>
 * Certifica la cadena completa inter-módulos del ERP:
 * <ol>
 *   <li>El motor de reposición ({@code replenishment}) emite un {@link NecesidadAbastecimientoDetectadaEvent}.</li>
 *   <li>El {@code NecesidadAbastecimientoEventListener} del módulo {@code purchasing} intercepta el evento de forma asíncrona.</li>
 *   <li>El caso de uso {@code CrearSolicitudAutomaticaService} persiste una {@code SolicitudAbastecimiento}
 *       en estado {@code BORRADOR} con aislamiento multi-tenant (MT-01).</li>
 * </ol>
 *
 * Protocolo-5 (MCP § 5): Los bounded contexts se comunican EXCLUSIVAMENTE a través de Domain Events.
 * Regla MT-01: Toda entidad persiste con su {@code empresa_id} como clave de partición.
 */
public class ReplenishmentToPurchasingIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    private SolicitudAbastecimientoRepository solicitudRepository;

    private UUID empresaId;
    private UUID bodegaId;
    private UUID productoId;

    @BeforeEach
    void setUp() {
        empresaId  = UUID.randomUUID();
        bodegaId   = UUID.randomUUID();
        productoId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe crear SolicitudAbastecimiento en BORRADOR cuando replenishment emite NecesidadAbastecimientoDetectadaEvent")
    void debeCrearSolicitudCuandoReplenishmentEmiteEvento() {
        // GIVEN: Un evento de necesidad de reabastecimiento del motor de reposición
        // Simula exactamente lo que emite PoliticaInventario.evaluarStock() cuando stock < puntoReorden
        NecesidadAbastecimientoDetectadaEvent evento = new NecesidadAbastecimientoDetectadaEvent(
                UUID.randomUUID(), // id del evento
                Instant.now(),     // ocurridoEn
                empresaId,
                bodegaId,
                productoId,
                45  // cantidadAReponer = nivelOptimo(50) - stockActual(5)
        );

        // WHEN: Se publica el evento en el bus de Spring (como lo haría el módulo replenishment)
        applicationEventPublisher.publishEvent(evento);

        // THEN: Esperamos (máximo 5 s) a que el listener asíncrono procese y persista la solicitud
        await().atMost(5, TimeUnit.SECONDS)
               .pollInterval(200, TimeUnit.MILLISECONDS)
               .untilAsserted(() -> {
                   // Consultar directamente en el repositorio con aislamiento MT-01
                   // La solicitud fue creada por el listener — buscamos por empresa, bodega y producto
                   // usando una consulta directa al adapter a través del repositorio

                   // Verificamos que exista al menos una solicitud para ese tenant en la BD
                   // La búsqueda directa se hace mediante el SpringData repository (accedido por el adapter)
                   boolean encontrada = solicitudExistePorEmpresaBodegaProducto(
                           empresaId, bodegaId, productoId);

                   assertThat(encontrada)
                           .as("Debe existir una SolicitudAbastecimiento persisitida en BD para empresa=%s, bodega=%s, producto=%s",
                               empresaId, bodegaId, productoId)
                           .isTrue();
               });
    }

    @Test
    @DisplayName("La SolicitudAbastecimiento creada automáticamente debe estar en estado BORRADOR con el tenant correcto")
    void solicitudAutomaticaDebeTenerEstadoBorradorYTenantCorrecto() {
        // GIVEN
        int cantidadAReponer = 30;
        NecesidadAbastecimientoDetectadaEvent evento = new NecesidadAbastecimientoDetectadaEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                bodegaId,
                productoId,
                cantidadAReponer
        );

        // WHEN
        applicationEventPublisher.publishEvent(evento);

        // THEN: Verificar todos los invariantes de la solicitud creada
        await().atMost(5, TimeUnit.SECONDS)
               .pollInterval(200, TimeUnit.MILLISECONDS)
               .untilAsserted(() -> {
                   Optional<SolicitudAbastecimiento> solicitudOpt =
                           buscarSolicitudReciente(empresaId, bodegaId, productoId);

                   assertThat(solicitudOpt)
                           .as("La solicitud debe existir en la base de datos")
                           .isPresent();

                   SolicitudAbastecimiento solicitud = solicitudOpt.get();

                   // MT-01: El tenant debe ser el del evento original
                   assertThat(solicitud.getEmpresaId().valor())
                           .as("El empresaId debe coincidir con el del evento (MT-01)")
                           .isEqualTo(empresaId);

                   // MT-01: La bodega debe ser la del evento
                   assertThat(solicitud.getBodegaId().valor())
                           .as("El bodegaId debe coincidir con el del evento")
                           .isEqualTo(bodegaId);

                   // BP-04: La solicitud automática se crea en BORRADOR (Four-Eyes Principle)
                   assertThat(solicitud.getEstado())
                           .as("La solicitud automática debe estar en BORRADOR (revisión humana requerida)")
                           .isEqualTo(EstadoSolicitud.BORRADOR);

                   // La solicitud debe tener exactamente 1 línea con el producto correcto
                   assertThat(solicitud.getLineas())
                           .as("Debe tener exactamente 1 línea de producto")
                           .hasSize(1);

                   assertThat(solicitud.getLineas().get(0).getProductoId().valor())
                           .as("El productoId de la línea debe coincidir con el del evento")
                           .isEqualTo(productoId);

                   assertThat(solicitud.getLineas().get(0).getCantidadSolicitada())
                           .as("La cantidad debe coincidir con cantidadAReponer del evento")
                           .isEqualByComparingTo(java.math.BigDecimal.valueOf(cantidadAReponer));
               });
    }

    @Test
    @DisplayName("Dos empresas distintas con el mismo producto NO deben interferir entre sí (aislamiento MT-01)")
    void dosEventosDistintosTenantNoDebenInterferir() {
        // GIVEN: Dos empresas distintas, mismo producto
        UUID empresa1 = UUID.randomUUID();
        UUID empresa2 = UUID.randomUUID();
        UUID mismoProducto = UUID.randomUUID();
        UUID mismaBodega = UUID.randomUUID();

        NecesidadAbastecimientoDetectadaEvent eventoEmpresa1 = new NecesidadAbastecimientoDetectadaEvent(
                UUID.randomUUID(), Instant.now(), empresa1, mismaBodega, mismoProducto, 20);

        NecesidadAbastecimientoDetectadaEvent eventoEmpresa2 = new NecesidadAbastecimientoDetectadaEvent(
                UUID.randomUUID(), Instant.now(), empresa2, mismaBodega, mismoProducto, 35);

        // WHEN: Ambos eventos se disparan
        applicationEventPublisher.publishEvent(eventoEmpresa1);
        applicationEventPublisher.publishEvent(eventoEmpresa2);

        // THEN: Cada empresa debe tener su propia solicitud independiente
        await().atMost(8, TimeUnit.SECONDS)
               .pollInterval(300, TimeUnit.MILLISECONDS)
               .untilAsserted(() -> {
                   boolean existeParaEmpresa1 = solicitudExistePorEmpresaBodegaProducto(empresa1, mismaBodega, mismoProducto);
                   boolean existeParaEmpresa2 = solicitudExistePorEmpresaBodegaProducto(empresa2, mismaBodega, mismoProducto);

                   assertThat(existeParaEmpresa1)
                           .as("Empresa 1 debe tener su propia solicitud")
                           .isTrue();
                   assertThat(existeParaEmpresa2)
                           .as("Empresa 2 debe tener su propia solicitud")
                           .isTrue();
               });
    }

    // -------------------------------------------------------------------------
    // Helpers de consulta — acceden al repositorio del módulo purchasing
    // -------------------------------------------------------------------------

    private boolean solicitudExistePorEmpresaBodegaProducto(UUID empresaId, UUID bodegaId, UUID productoId) {
        return buscarSolicitudReciente(empresaId, bodegaId, productoId).isPresent();
    }

    /**
     * Busca la solicitud más reciente para una empresa/bodega/producto vía el repositorio.
     * En producción real habría un método de consulta específico; aquí usamos el adapter
     * que nos proporciona el repositorio base para verificación de integración.
     */
    private Optional<SolicitudAbastecimiento> buscarSolicitudReciente(UUID empresaId, UUID bodegaId, UUID productoId) {
        // Como el repositorio del dominio solo expone buscarPorIdYEmpresa (sin el ID),
        // usamos una verificación indirecta a través del adapter de persistencia extendido.
        // Este es el punto de extensión que el módulo purchasing necesita para queries de read.
        // Para los tests, inyectamos directamente la implementación JPA.
        if (solicitudRepository instanceof com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.SolicitudJpaAdapter adapter) {
            return adapter.buscarMasRecientePorEmpresaBodegaProducto(
                    new EmpresaId(empresaId),
                    new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.BodegaId(bodegaId),
                    new com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId(productoId)
            );
        }
        return Optional.empty();
    }
}
