package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.event.MercanciaRecibidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecepcionMercanciaTest {

    private EmpresaId empresaId;
    private RecepcionId recepcionId;
    private OrdenCompraId ordenCompraId;
    private BodegaId bodegaId;
    private ProductoId productoId;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        recepcionId = RecepcionId.generar();
        ordenCompraId = new OrdenCompraId(UUID.randomUUID());
        bodegaId = new BodegaId(UUID.randomUUID());
        productoId = new ProductoId(UUID.randomUUID());
    }

    @Test
    void testCrearRecepcionPlanificada() {
        List<LineaRecepcion> lineas = new ArrayList<>();
        lineas.add(new LineaRecepcion(productoId, new CantidadRecepcion(100)));

        RecepcionMercancia recepcion = new RecepcionMercancia(empresaId, recepcionId, ordenCompraId, bodegaId, lineas);

        assertEquals(EstadoRecepcion.PLANIFICADA, recepcion.getEstado());
        assertEquals(1, recepcion.getLineas().size());
        assertEquals(0, recepcion.getLineas().get(0).getCantidadRecibida().valor());
    }

    @Test
    void testRecibirProductoTransicionaAEnProceso() {
        List<LineaRecepcion> lineas = new ArrayList<>();
        lineas.add(new LineaRecepcion(productoId, new CantidadRecepcion(100)));

        RecepcionMercancia recepcion = new RecepcionMercancia(empresaId, recepcionId, ordenCompraId, bodegaId, lineas);

        recepcion.recibirProducto(productoId, new CantidadRecepcion(50));

        assertEquals(EstadoRecepcion.EN_PROCESO, recepcion.getEstado());
        assertEquals(50, recepcion.getLineas().get(0).getCantidadRecibida().valor());
    }

    @Test
    void testRecibirProductoExcediendoCantidadFalla() {
        List<LineaRecepcion> lineas = new ArrayList<>();
        lineas.add(new LineaRecepcion(productoId, new CantidadRecepcion(100)));

        RecepcionMercancia recepcion = new RecepcionMercancia(empresaId, recepcionId, ordenCompraId, bodegaId, lineas);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            recepcion.recibirProducto(productoId, new CantidadRecepcion(150))
        );

        assertTrue(ex.getMessage().contains("Sobre-entrega rechazada"));
    }

    @Test
    void testCompletarRecepcionEmiteEvento() {
        List<LineaRecepcion> lineas = new ArrayList<>();
        lineas.add(new LineaRecepcion(productoId, new CantidadRecepcion(100)));

        RecepcionMercancia recepcion = new RecepcionMercancia(empresaId, recepcionId, ordenCompraId, bodegaId, lineas);
        
        recepcion.recibirProducto(productoId, new CantidadRecepcion(100));
        recepcion.completar();

        assertEquals(EstadoRecepcion.COMPLETADA, recepcion.getEstado());

        // Verificar que emite el evento de dominio
        var eventos = recepcion.obtenerEventosDominio();
        assertEquals(1, eventos.size());
        
        var evento = eventos.iterator().next();
        assertTrue(evento instanceof MercanciaRecibidaEvent);
        
        MercanciaRecibidaEvent mRevent = (MercanciaRecibidaEvent) evento;
        assertEquals(empresaId.valor(), mRevent.empresaId().valor());
        assertEquals(recepcionId.valor(), mRevent.recepcionId().valor());
        assertEquals(1, mRevent.lineasRecibidas().size());
        assertEquals(100, mRevent.lineasRecibidas().get(0).getCantidadRecibida().valor());
    }

    @Test
    void testRecibirEnRecepcionCompletadaFalla() {
        List<LineaRecepcion> lineas = new ArrayList<>();
        lineas.add(new LineaRecepcion(productoId, new CantidadRecepcion(100)));

        RecepcionMercancia recepcion = new RecepcionMercancia(empresaId, recepcionId, ordenCompraId, bodegaId, lineas);
        recepcion.completar();

        assertThrows(IllegalStateException.class, () -> 
            recepcion.recibirProducto(productoId, new CantidadRecepcion(50))
        );
    }
}
