package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Dominio: Value Object Dinero")
class DineroTest {

    @Test
    @DisplayName("Debe instanciar correctamente con escala y moneda")
    void debeInstanciarCorrectamente() {
        Dinero dinero = Dinero.de(new BigDecimal("10.5"), "usd");

        assertEquals(new BigDecimal("10.50"), dinero.monto());
        assertEquals("USD", dinero.moneda());
        assertEquals("USD 10.50", dinero.toString());
    }

    @Test
    @DisplayName("Debe sumar y restar montos de la misma moneda")
    void debeSumarYRestar() {
        Dinero d1 = Dinero.de(50.0);
        Dinero d2 = Dinero.de(25.5);

        Dinero suma = d1.sumar(d2);
        assertEquals(new BigDecimal("75.50"), suma.monto());

        Dinero resta = d1.restar(d2);
        assertEquals(new BigDecimal("24.50"), resta.monto());
    }

    @Test
    @DisplayName("Debe multiplicar por cantidades enteras")
    void debeMultiplicar() {
        Dinero d = Dinero.de(12.50);
        Dinero resultado = d.multiplicar(3);

        assertEquals(new BigDecimal("37.50"), resultado.monto());
    }

    @Test
    @DisplayName("Debe rechazar operaciones entre monedas diferentes")
    void debeRechazarMonedasDiferentes() {
        Dinero usd = Dinero.de(100, "USD");
        Dinero cop = Dinero.de(100, "COP");

        assertThrows(IllegalArgumentException.class, () -> usd.sumar(cop));
        assertThrows(IllegalArgumentException.class, () -> usd.restar(cop));
    }

    @Test
    @DisplayName("Debe rechazar montos negativos o null")
    void debeRechazarMontosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> Dinero.de((BigDecimal) null));
        assertThrows(IllegalArgumentException.class, () -> Dinero.de(-10.0));
    }
}
