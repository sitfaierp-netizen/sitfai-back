package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Dominio: Entidad Local ItemPedido (Api_Tienda)")
class ItemPedidoTest {

    private final ProductoId productoId = ProductoId.generar();
    private final Dinero precioUnitario = Dinero.de(15.50);

    @Nested
    @DisplayName("Creación y Cálculos")
    class CreacionTests {

        @Test
        @DisplayName("Debe crear ItemPedido y calcular subtotal correctamente")
        void debeCrearItemYCalcularSubtotal() {
            ItemPedido item = ItemPedido.crear(productoId, Cantidad.de(3), precioUnitario);

            assertThat(item.getId()).isNotNull();
            assertThat(item.getProductoId()).isEqualTo(productoId);
            assertThat(item.getCantidad().valor()).isEqualTo(3);
            assertThat(item.getPrecioUnitario()).isEqualTo(precioUnitario);
            assertThat(item.subtotal().monto()).isEqualByComparingTo(new BigDecimal("46.50"));
        }

        @Test
        @DisplayName("Debe incrementar cantidad con objeto Cantidad")
        void debeIncrementarCantidadVO() {
            ItemPedido item = ItemPedido.crear(productoId, Cantidad.de(2), precioUnitario);
            item.aumentarCantidad(Cantidad.de(3));

            assertThat(item.getCantidad().valor()).isEqualTo(5);
            assertThat(item.subtotal().monto()).isEqualByComparingTo(new BigDecimal("77.50"));
        }

        @Test
        @DisplayName("Debe incrementar cantidad con enteros")
        void debeIncrementarCantidadEntero() {
            ItemPedido item = ItemPedido.crear(productoId, 2, precioUnitario);
            item.aumentarCantidad(4);

            assertThat(item.getCantidad().valor()).isEqualTo(6);
        }

        @Test
        @DisplayName("Debe fallar si los atributos obligatorios son null")
        void debeFallarConAtributosNull() {
            assertThrows(NullPointerException.class, () ->
                    new ItemPedido(null, productoId, Cantidad.uno(), precioUnitario));
            assertThrows(NullPointerException.class, () ->
                    new ItemPedido(UUID.randomUUID(), null, Cantidad.uno(), precioUnitario));
            assertThrows(NullPointerException.class, () ->
                    new ItemPedido(UUID.randomUUID(), productoId, null, precioUnitario));
            assertThrows(NullPointerException.class, () ->
                    new ItemPedido(UUID.randomUUID(), productoId, Cantidad.uno(), null));
        }
    }
}
