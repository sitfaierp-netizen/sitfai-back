package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DespachoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias puras de Dominio para el Aggregate Root Despacho (Outbound Logistics).
 * <p>
 * Regla 1 (Aislamiento): Cero dependencias de Spring, Hibernate o Mockito.
 * Regla 3 (DDD): Validación de transiciones de estado, invariantes y emisión de eventos.
 * Reglas MT-01 y BOD-04: Blindaje multitenant y documento fuente.
 */
class DespachoTest {

    private EmpresaId empresaId;
    private DespachoId despachoId;
    private PedidoId pedidoId;
    private ProductoId productoId;
    private Cantidad cantidad;

    @BeforeEach
    void setUp() {
        empresaId = EmpresaId.de(UUID.randomUUID());
        despachoId = DespachoId.generar();
        pedidoId = PedidoId.de(UUID.randomUUID());
        productoId = ProductoId.de(UUID.randomUUID());
        cantidad = Cantidad.de(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Debe instanciar un despacho nuevo en estado PENDIENTE con invariantes válidas")
    void crear_despachoValido_iniciaEnEstadoPendiente() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);

        assertThat(despacho.getId()).isEqualTo(despachoId);
        assertThat(despacho.getEmpresaId()).isEqualTo(empresaId);
        assertThat(despacho.getPedidoId()).isEqualTo(pedidoId);
        assertThat(despacho.getEstado()).isEqualTo(EstadoDespacho.PENDIENTE);
        assertThat(despacho.getLineas()).isEmpty();
        assertThat(despacho.getCreatedAt()).isNotNull();
        assertThat(despacho.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe fallar al instanciar con parámetros nulos respetando fail-fast y MT-01 / BOD-04")
    void crear_conParametrosNulos_lanzaNullPointerException() {
        assertThatThrownBy(() -> Despacho.crear(null, empresaId, pedidoId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id es obligatorio");

        assertThatThrownBy(() -> Despacho.crear(despachoId, null, pedidoId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("empresaId es obligatorio");

        assertThatThrownBy(() -> Despacho.crear(despachoId, empresaId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("pedidoId es obligatorio");
    }

    @Test
    @DisplayName("Debe permitir agregar líneas en estado PENDIENTE y proteger inmutabilidad de la colección")
    void agregarLinea_enEstadoPendiente_agregaLineaYProtegeEncapsulamiento() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);
        LineaDespacho linea = new LineaDespacho(productoId, cantidad);

        despacho.agregarLinea(linea);

        assertThat(despacho.getLineas()).hasSize(1);
        assertThat(despacho.getLineas().get(0).getProductoId()).isEqualTo(productoId);
        assertThat(despacho.getLineas().get(0).getCantidad()).isEqualTo(cantidad);

        // Protección de encapsulamiento: no debe poder modificarse externamente
        assertThatThrownBy(() -> despacho.getLineas().add(new LineaDespacho(productoId, cantidad)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Debe transicionar el flujo logístico PENDIENTE -> EN_PICKING -> EMPACADO")
    void transicionesDeEstado_flujoLogisticoCompleto() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);
        despacho.agregarLinea(new LineaDespacho(productoId, cantidad));

        despacho.iniciarPicking();
        assertThat(despacho.getEstado()).isEqualTo(EstadoDespacho.EN_PICKING);

        despacho.empacar();
        assertThat(despacho.getEstado()).isEqualTo(EstadoDespacho.EMPACADO);
    }

    @Test
    @DisplayName("Debe fallar al iniciar picking si no tiene líneas asignadas")
    void iniciarPicking_sinLineas_lanzaIllegalStateException() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);

        assertThatThrownBy(despacho::iniciarPicking)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sin líneas asignadas");
    }

    @Test
    @DisplayName("Debe rechazar agregar líneas cuando el despacho ya no está en estado PENDIENTE")
    void agregarLinea_enEstadoNoPendiente_lanzaIllegalStateException() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);
        despacho.agregarLinea(new LineaDespacho(productoId, cantidad));
        despacho.iniciarPicking();

        assertThatThrownBy(() -> despacho.agregarLinea(new LineaDespacho(productoId, Cantidad.de(5))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pueden agregar líneas a un despacho en estado: EN_PICKING");
    }

    @Test
    @DisplayName("Debe confirmar despacho exitosamente, cambiar estado a DESPACHADO y emitir DespachoConfirmadoEvent")
    void confirmarDespacho_conLineas_transicionaADespachadoYEmitirEvento() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);
        despacho.agregarLinea(new LineaDespacho(productoId, cantidad));

        despacho.confirmarDespacho();

        assertThat(despacho.getEstado()).isEqualTo(EstadoDespacho.DESPACHADO);

        List<DomainEvent> events = despacho.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(DespachoConfirmadoEvent.class);

        DespachoConfirmadoEvent event = (DespachoConfirmadoEvent) events.get(0);
        assertThat(event.despachoId()).isEqualTo(despachoId);
        assertThat(event.empresaId()).isEqualTo(empresaId);
        assertThat(event.pedidoId()).isEqualTo(pedidoId);
        assertThat(event.lineas()).hasSize(1);
        assertThat(event.lineas().get(0).productoId()).isEqualTo(productoId);
        assertThat(event.lineas().get(0).cantidad()).isEqualTo(cantidad);

        // pullDomainEvents drena la lista
        assertThat(despacho.pullDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Debe fallar (fail-fast) al confirmar un despacho sin líneas")
    void confirmarDespacho_sinLineas_lanzaIllegalStateException() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);

        assertThatThrownBy(despacho::confirmarDespacho)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sin líneas asignadas");
    }

    @Test
    @DisplayName("Debe fallar al intentar confirmar un despacho ya despachado")
    void confirmarDespacho_yaDespachado_lanzaIllegalStateException() {
        Despacho despacho = Despacho.crear(despachoId, empresaId, pedidoId);
        despacho.agregarLinea(new LineaDespacho(productoId, cantidad));
        despacho.confirmarDespacho();

        assertThatThrownBy(despacho::confirmarDespacho)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya ha sido confirmado y despachado");
    }

    @Test
    @DisplayName("Value Objects: DespachoId y PedidoId validan inmutabilidad y formato UUID")
    void valueObjects_validacionesEstrictas() {
        UUID rawUuid = UUID.randomUUID();

        DespachoId dId = DespachoId.de(rawUuid.toString());
        assertThat(dId.valor()).isEqualTo(rawUuid);

        PedidoId pId = PedidoId.de(rawUuid.toString());
        assertThat(pId.valor()).isEqualTo(rawUuid);

        assertThatThrownBy(() -> DespachoId.de((UUID) null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> PedidoId.de((UUID) null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> DespachoId.de("invalid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> PedidoId.de("invalid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
