package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.event.RecetaProduccionAprobadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListaMaterialesTest {

    private EmpresaId empresaId;
    private RecetaId recetaId;
    private ProductoFinalId productoFinalId;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        recetaId = new RecetaId(UUID.randomUUID());
        productoFinalId = new ProductoFinalId(UUID.randomUUID());
    }

    @Test
    void debeCrearListaMaterialesEnBorrador() {
        ListaMateriales bom = new ListaMateriales(empresaId, recetaId, productoFinalId);

        assertThat(bom.getEstado()).isEqualTo(EstadoReceta.BORRADOR);
        assertThat(bom.getEmpresaId()).isEqualTo(empresaId);
        assertThat(bom.getComponentes()).isEmpty();
    }

    @Test
    void debeRechazarCantidadNegativaOCeroEnInsumo() {
        assertThatThrownBy(() -> new CantidadInsumo(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La cantidad debe ser mayor a cero");

        assertThatThrownBy(() -> new CantidadInsumo(new BigDecimal("-1.5")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La cantidad debe ser mayor a cero");
    }

    @Test
    void debeAgregarComponenteExitosamente() {
        ListaMateriales bom = new ListaMateriales(empresaId, recetaId, productoFinalId);
        InsumoId insumoId = new InsumoId(UUID.randomUUID());
        CantidadInsumo cantidad = new CantidadInsumo(new BigDecimal("10.5"));

        bom.agregarComponente(insumoId, cantidad);

        assertThat(bom.getComponentes()).hasSize(1);
        assertThat(bom.getComponentes().get(0).getInsumoId()).isEqualTo(insumoId);
        assertThat(bom.getComponentes().get(0).getCantidad()).isEqualTo(cantidad);
    }

    @Test
    void noDebeAprobarSiListaEstaVacia() {
        ListaMateriales bom = new ListaMateriales(empresaId, recetaId, productoFinalId);

        assertThatThrownBy(bom::aprobar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No se puede aprobar una receta sin componentes");
    }

    @Test
    void debeAprobarYEmitirEvento() {
        ListaMateriales bom = new ListaMateriales(empresaId, recetaId, productoFinalId);
        bom.agregarComponente(new InsumoId(UUID.randomUUID()), new CantidadInsumo(new BigDecimal("5.0")));

        bom.aprobar();

        assertThat(bom.getEstado()).isEqualTo(EstadoReceta.APROBADA);
        assertThat(bom.obtenerEventosDominio()).hasSize(1);
        
        Object event = bom.obtenerEventosDominio().iterator().next();
        assertThat(event).isInstanceOf(RecetaProduccionAprobadaEvent.class);
        RecetaProduccionAprobadaEvent recetaEvent = (RecetaProduccionAprobadaEvent) event;
        assertThat(recetaEvent.empresaId()).isEqualTo(empresaId);
        assertThat(recetaEvent.recetaId()).isEqualTo(recetaId);
    }

    @Test
    void noDebePermitirAgregarComponentesSiYaEstaAprobada() {
        ListaMateriales bom = new ListaMateriales(empresaId, recetaId, productoFinalId);
        bom.agregarComponente(new InsumoId(UUID.randomUUID()), new CantidadInsumo(new BigDecimal("5.0")));
        bom.aprobar();

        InsumoId nuevoInsumo = new InsumoId(UUID.randomUUID());
        CantidadInsumo nuevaCantidad = new CantidadInsumo(new BigDecimal("2.0"));

        assertThatThrownBy(() -> bom.agregarComponente(nuevoInsumo, nuevaCantidad))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden agregar componentes si la receta est");
    }
}
