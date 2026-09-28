package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.event.DespachoCompletadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.ProductoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrdenDespachoTest {

    @Test
    @DisplayName("Debe crear una orden de despacho en estado PENDIENTE")
    void crearOrdenDespacho() {
        EmpresaId empresaId = EmpresaId.de(UUID.randomUUID());
        DespachoId despachoId = DespachoId.generar();
        PedidoId pedidoId = PedidoId.de(UUID.randomUUID());
        BodegaId bodegaId = BodegaId.de(UUID.randomUUID());
        List<LineaDespacho> lineas = List.of(new LineaDespacho(ProductoId.de(UUID.randomUUID()), 5));

        OrdenDespacho orden = OrdenDespacho.crear(empresaId, despachoId, pedidoId, bodegaId, lineas);

        assertNotNull(orden);
        assertEquals(EstadoDespacho.PENDIENTE, orden.getEstado());
        assertEquals(1, orden.getLineas().size());
        assertTrue(orden.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Transición completa del ciclo de vida de una orden de despacho")
    void cicloDeVidaCompleto() {
        EmpresaId empresaId = EmpresaId.de(UUID.randomUUID());
        OrdenDespacho orden = OrdenDespacho.crear(empresaId, DespachoId.generar(), PedidoId.de(UUID.randomUUID()), BodegaId.de(UUID.randomUUID()), List.of(new LineaDespacho(ProductoId.de(UUID.randomUUID()), 2)));

        assertEquals(EstadoDespacho.PENDIENTE, orden.getEstado());

        orden.iniciarPicking();
        assertEquals(EstadoDespacho.EN_PICKING, orden.getEstado());

        orden.completarEmpaque();
        assertEquals(EstadoDespacho.EMPACADO, orden.getEstado());

        orden.confirmarDespacho();
        assertEquals(EstadoDespacho.DESPACHADO, orden.getEstado());

        List<Object> events = orden.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof DespachoCompletadoEvent);
        DespachoCompletadoEvent event = (DespachoCompletadoEvent) events.get(0);
        assertEquals(empresaId, event.empresaId());
    }

    @Test
    @DisplayName("No debe permitir empacar si no está en picking")
    void invarianteEmpaque() {
        EmpresaId empresaId = EmpresaId.de(UUID.randomUUID());
        OrdenDespacho orden = OrdenDespacho.crear(empresaId, DespachoId.generar(), PedidoId.de(UUID.randomUUID()), BodegaId.de(UUID.randomUUID()), List.of(new LineaDespacho(ProductoId.de(UUID.randomUUID()), 2)));

        IllegalStateException exception = assertThrows(IllegalStateException.class, orden::completarEmpaque);
        assertEquals("Solo se puede empacar si el despacho está EN_PICKING.", exception.getMessage());
    }

    @Test
    @DisplayName("No debe permitir despachar si no está empacado")
    void invarianteDespacho() {
        EmpresaId empresaId = EmpresaId.de(UUID.randomUUID());
        OrdenDespacho orden = OrdenDespacho.crear(empresaId, DespachoId.generar(), PedidoId.de(UUID.randomUUID()), BodegaId.de(UUID.randomUUID()), List.of(new LineaDespacho(ProductoId.de(UUID.randomUUID()), 2)));

        orden.iniciarPicking();

        IllegalStateException exception = assertThrows(IllegalStateException.class, orden::confirmarDespacho);
        assertEquals("Solo se puede confirmar despacho si el despacho está EMPACADO.", exception.getMessage());
    }
}
