package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Dominio: Value Object Cantidad (Api_Tienda)")
class CantidadTest {

    @Nested
    @DisplayName("Creación e Invariantes")
    class CreacionTests {

        @Test
        @DisplayName("Debe crear cantidad válida positiva")
        void debeCrearCantidadValida() {
            Cantidad c = Cantidad.de(5);
            assertThat(c.valor()).isEqualTo(5);
            assertThat(c.aBigDecimal()).isEqualByComparingTo("5");
            assertThat(c.toString()).isEqualTo("5");
        }

        @Test
        @DisplayName("Debe crear cantidad desde BigDecimal")
        void debeCrearCantidadDesdeBigDecimal() {
            Cantidad c = Cantidad.de(new BigDecimal("10"));
            assertThat(c.valor()).isEqualTo(10);
        }

        @Test
        @DisplayName("Debe crear cantidad uno")
        void debeCrearCantidadUno() {
            Cantidad c = Cantidad.uno();
            assertThat(c.valor()).isEqualTo(1);
        }

        @Test
        @DisplayName("Debe fallar si el valor entero es 0 o negativo")
        void debeFallarConValorInvalidoEntero() {
            assertThrows(IllegalArgumentException.class, () -> Cantidad.de(0));
            assertThrows(IllegalArgumentException.class, () -> Cantidad.de(-3));
        }

        @Test
        @DisplayName("Debe fallar si el valor BigDecimal es null, 0 o negativo")
        void debeFallarConValorInvalidoBigDecimal() {
            assertThrows(NullPointerException.class, () -> Cantidad.de((BigDecimal) null));
            assertThrows(IllegalArgumentException.class, () -> Cantidad.de(BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () -> Cantidad.de(new BigDecimal("-1")));
        }
    }

    @Nested
    @DisplayName("Operaciones Aritméticas")
    class OperacionesTests {

        @Test
        @DisplayName("Debe sumar otra cantidad")
        void debeSumarOtraCantidad() {
            Cantidad c1 = Cantidad.de(3);
            Cantidad c2 = Cantidad.de(4);
            Cantidad resultado = c1.sumar(c2);

            assertThat(resultado.valor()).isEqualTo(7);
        }

        @Test
        @DisplayName("Debe sumar unidades enteras")
        void debeSumarUnidadesEnteras() {
            Cantidad c1 = Cantidad.de(3);
            Cantidad resultado = c1.sumar(2);

            assertThat(resultado.valor()).isEqualTo(5);
        }

        @Test
        @DisplayName("Debe fallar al sumar unidades menores o iguales a cero")
        void debeFallarSumarInvalido() {
            Cantidad c1 = Cantidad.de(3);
            assertThrows(IllegalArgumentException.class, () -> c1.sumar(0));
            assertThrows(IllegalArgumentException.class, () -> c1.sumar(-1));
        }
    }
}
