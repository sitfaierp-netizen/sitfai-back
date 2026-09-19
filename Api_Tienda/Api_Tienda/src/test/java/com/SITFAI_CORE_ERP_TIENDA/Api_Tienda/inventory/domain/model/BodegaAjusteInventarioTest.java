package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BodegaAjusteInventarioTest {

    private Bodega bodega;
    private EmpresaId empresaId;
    private SucursalId sucursalId;
    private ProductoId productoId;
    private DocumentoFuenteId docInicial;
    private DocumentoFuenteId docAjuste;
    private LoteId loteId;

    @BeforeEach
    void setUp() {
        empresaId = EmpresaId.de(UUID.randomUUID());
        sucursalId = SucursalId.de(UUID.randomUUID());
        productoId = ProductoId.de(UUID.randomUUID());
        docInicial = new DocumentoFuenteId("RECEPCION", UUID.randomUUID().toString());
        docAjuste = new DocumentoFuenteId("AJUSTE", UUID.randomUUID().toString());
        loteId = LoteId.de("LOTE-123");

        bodega = Bodega.crear(empresaId, sucursalId, "BDG-001", "Bodega Principal");
    }

    @Test
    void aplicarAjuste_cuandoDiferenciaPositiva_debeIncrementarStock() {
        // Ejecución
        bodega.aplicarAjuste(productoId, new BigDecimal("10.5"), loteId, Instant.now(), docAjuste);

        // Verificación
        BigDecimal stockActual = bodega.consultarStock(productoId);
        assertEquals(new BigDecimal("10.5"), stockActual);
    }

    @Test
    void aplicarAjuste_cuandoDiferenciaNegativaConLote_debeDescontarStockExacto() {
        // Preparación: Ingresar 20 unidades en LOTE-123
        bodega.registrarIngreso(productoId, Cantidad.de(20), loteId, Instant.now(), docInicial);

        // Ejecución: Faltante de 5 unidades en LOTE-123
        bodega.aplicarAjuste(productoId, new BigDecimal("-5.0"), loteId, null, docAjuste);

        // Verificación
        BigDecimal stockActual = bodega.consultarStock(productoId);
        assertEquals(new BigDecimal("15.0"), stockActual);
    }

    @Test
    void aplicarAjuste_cuandoDiferenciaNegativaSinLote_debeDescontarStockPorFefo() {
        // Preparación: Ingresar 20 unidades sin lote especificado (toma lote por defecto)
        bodega.registrarIngreso(productoId, Cantidad.de(20), null, null, docInicial);

        // Ejecución: Faltante de 8 unidades (no se especifica lote, usa FEFO)
        bodega.aplicarAjuste(productoId, new BigDecimal("-8.0"), null, null, docAjuste);

        // Verificación
        BigDecimal stockActual = bodega.consultarStock(productoId);
        assertEquals(new BigDecimal("12.0"), stockActual);
    }
}
