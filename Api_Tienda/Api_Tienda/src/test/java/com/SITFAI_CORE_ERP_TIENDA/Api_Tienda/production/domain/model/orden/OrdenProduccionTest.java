package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.event.ProduccionCompletadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.event.ProduccionIniciadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdenProduccionTest {

    private EmpresaId empresaId;
    private OrdenProduccionId ordenId;
    private RecetaId recetaId;
    private BodegaId bodegaId;
    private CantidadProducir cantidad;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        ordenId = new OrdenProduccionId(UUID.randomUUID());
        recetaId = new RecetaId(UUID.randomUUID());
        bodegaId = new BodegaId(UUID.randomUUID());
        cantidad = new CantidadProducir(100);
    }

    @Test
    void debeCrearOrdenPlanificada() {
        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);

        assertThat(orden.getEstado()).isEqualTo(EstadoOrdenProduccion.PLANIFICADA);
        assertThat(orden.getEmpresaId()).isEqualTo(empresaId);
    }

    @Test
    void debeRechazarCantidadNegativaOCero() {
        assertThatThrownBy(() -> new CantidadProducir(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La cantidad a producir debe ser mayor a cero");

        assertThatThrownBy(() -> new CantidadProducir(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La cantidad a producir debe ser mayor a cero");
    }

    @Test
    void debeIniciarProduccionYEmitirEvento() {
        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);

        orden.iniciarProduccion();

        assertThat(orden.getEstado()).isEqualTo(EstadoOrdenProduccion.EN_PROGRESO);
        
        Object event = orden.obtenerEventosDominio().iterator().next();
        assertThat(event).isInstanceOf(ProduccionIniciadaEvent.class);
    }

    @Test
    void noDebeIniciarProduccionSiYaEstaEnProgreso() {
        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);
        orden.iniciarProduccion();

        assertThatThrownBy(orden::iniciarProduccion)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Solo se puede iniciar una orden que est en estado PLANIFICADA");
    }

    @Test
    void debeCompletarProduccionYEmitirEvento() {
        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);
        orden.iniciarProduccion();
        orden.limpiarEventosDominio(); // Limpiar eventos anteriores

        orden.completarProduccion();

        assertThat(orden.getEstado()).isEqualTo(EstadoOrdenProduccion.COMPLETADA);
        
        Object event = orden.obtenerEventosDominio().iterator().next();
        assertThat(event).isInstanceOf(ProduccionCompletadaEvent.class);
    }

    @Test
    void noDebeCompletarProduccionSiEstaPlanificada() {
        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);

        assertThatThrownBy(orden::completarProduccion)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Solo se puede completar una orden que est en estado EN_PROGRESO");
    }

    @Test
    void debeCancelarProduccion() {
        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);

        orden.cancelarProduccion();

        assertThat(orden.getEstado()).isEqualTo(EstadoOrdenProduccion.CANCELADA);
    }

    @Test
    void noDebeCancelarSiEstaCompletada() {
        OrdenProduccion orden = new OrdenProduccion(empresaId, ordenId, recetaId, bodegaId, cantidad);
        orden.iniciarProduccion();
        orden.completarProduccion();

        assertThatThrownBy(orden::cancelarProduccion)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No se puede cancelar una orden que ya fue COMPLETADA");
    }
}
