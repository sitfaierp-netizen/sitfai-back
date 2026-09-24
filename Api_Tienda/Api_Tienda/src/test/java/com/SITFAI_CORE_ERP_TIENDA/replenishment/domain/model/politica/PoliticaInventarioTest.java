package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.event.NecesidadAbastecimientoDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.NivelOptimo;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PuntoReorden;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaInventarioTest {

    private final PoliticaId politicaId = new PoliticaId(UUID.randomUUID());
    private final EmpresaId empresaId = new EmpresaId(UUID.randomUUID());
    private final BodegaId bodegaId = new BodegaId(UUID.randomUUID());
    private final ProductoId productoId = new ProductoId(UUID.randomUUID());

    @Test
    void debeRegistrarEventoSiStockEsMenorOIgualAlPuntoReordenYPoliticaActiva() {
        PuntoReorden puntoReorden = new PuntoReorden(10);
        NivelOptimo nivelOptimo = new NivelOptimo(50);
        PoliticaInventario politica = new PoliticaInventario(politicaId, empresaId, bodegaId, productoId, puntoReorden, nivelOptimo, true);

        politica.evaluarStock(5); // 5 <= 10, Reposición: 50 - 5 = 45

        List<Object> events = politica.pullDomainEvents();
        assertEquals(1, events.size());
        
        NecesidadAbastecimientoDetectadaEvent event = (NecesidadAbastecimientoDetectadaEvent) events.get(0);
        assertEquals(45, event.cantidadAReponer());
        assertEquals(empresaId.valor(), event.empresaId());
        assertEquals(bodegaId.valor(), event.bodegaId());
        assertEquals(productoId.valor(), event.productoId());
    }

    @Test
    void noDebeRegistrarEventoSiStockEsMayorAlPuntoReorden() {
        PuntoReorden puntoReorden = new PuntoReorden(10);
        NivelOptimo nivelOptimo = new NivelOptimo(50);
        PoliticaInventario politica = new PoliticaInventario(politicaId, empresaId, bodegaId, productoId, puntoReorden, nivelOptimo, true);

        politica.evaluarStock(11); // 11 > 10

        List<Object> events = politica.pullDomainEvents();
        assertTrue(events.isEmpty());
    }

    @Test
    void noDebeRegistrarEventoSiPoliticaEstaInactiva() {
        PuntoReorden puntoReorden = new PuntoReorden(10);
        NivelOptimo nivelOptimo = new NivelOptimo(50);
        PoliticaInventario politica = new PoliticaInventario(politicaId, empresaId, bodegaId, productoId, puntoReorden, nivelOptimo, false);

        politica.evaluarStock(5); // 5 <= 10, pero política está inactiva

        List<Object> events = politica.pullDomainEvents();
        assertTrue(events.isEmpty());
    }

    @Test
    void debeLanzarExcepcionSiNivelOptimoNoEsMayorAPuntoReorden() {
        PuntoReorden puntoReorden = new PuntoReorden(50);
        NivelOptimo nivelOptimo = new NivelOptimo(50);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PoliticaInventario(politicaId, empresaId, bodegaId, productoId, puntoReorden, nivelOptimo, true);
        });

        assertEquals("El Nivel Óptimo debe ser estricatamente mayor al Punto de Reorden", exception.getMessage());
    }

    @Test
    void debeLanzarExcepcionSiPuntoReordenEsNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new PuntoReorden(-1));
    }

    @Test
    void debeLanzarExcepcionSiNivelOptimoEsCeroOMenor() {
        assertThrows(IllegalArgumentException.class, () -> new NivelOptimo(0));
        assertThrows(IllegalArgumentException.class, () -> new NivelOptimo(-5));
    }
}
