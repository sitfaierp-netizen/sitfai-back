package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DevolucionRegistradaEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.LoteRevertido;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class ReingresoStockDevolucionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BodegaRepository bodegaRepository;

    @Test
    void debeReingresarStockCuandoSePublicaDevolucionRegistradaEvent() {
        // 1. Configurar datos base (Bodega)
        UUID empresaId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        String lote = "LOTE-REV-1";

        Bodega bodega = Bodega.crear(
                new EmpresaId(empresaId),
                new SucursalId(sucursalId),
                "BOD-TEST-DEV",
                "Bodega Devoluciones",
                TipoBodega.VENTA
        );
        bodegaRepository.guardar(bodega);

        // 2. Simular publicación del evento por el módulo POS
        LoteRevertido loteRevertido = new LoteRevertido(
                new com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId(productoId),
                lote,
                new BigDecimal("5.00")
        );

        DevolucionRegistradaEvent event = DevolucionRegistradaEvent.of(
                new TurnoId(UUID.randomUUID()),
                new CajaId(sucursalId),
                new com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId(empresaId),
                UUID.randomUUID(),
                List.of(), // no necesitamos simular lineas para este test de lotes
                List.of(loteRevertido),
                Dinero.de(new BigDecimal("50.00"))
        );

        applicationEventPublisher.publishEvent(event);

        // 3. Verificar que el stock se haya incrementado
        Bodega bodegaActualizada = bodegaRepository.buscarPorId(bodega.getId(), new EmpresaId(empresaId)).orElseThrow();
        BigDecimal stockProducto = bodegaActualizada.consultarStock(new ProductoId(productoId));
        
        assertEquals(0, stockProducto.compareTo(new BigDecimal("5.00")), "El stock debe ser 5.00 tras la devolución");
    }
}
