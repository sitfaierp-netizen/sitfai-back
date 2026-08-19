package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    private final ProductoId productoId = ProductoId.de(UUID.randomUUID().toString());
    private final EmpresaId empresaId = EmpresaId.de(UUID.randomUUID());
    private final CategoriaId categoriaId = CategoriaId.de(UUID.randomUUID().toString());

    @Test
    void debeCrearProductoValido() {
        Producto producto = Producto.crear(
                productoId, empresaId, "SKU-001", "Nombre Prod", "Desc",
                categoriaId, UnidadMedida.UNIDAD, null, new BigDecimal("100.00"),
                Impuesto.IVA_19, "123456789"
        );
        
        assertNotNull(producto);
        assertEquals("SKU-001", producto.getSku());
        assertNull(producto.getPrecioCompra());
        assertEquals(EstadoProducto.ACTIVO, producto.getEstado());
    }

    @Test
    void precioVentaCeroONegativoLanzaExcepcion() {
        DomainException ex = assertThrows(DomainException.class, () -> 
            Producto.crear(
                productoId, empresaId, "SKU-001", "Nombre Prod", "Desc",
                categoriaId, UnidadMedida.UNIDAD, null, BigDecimal.ZERO,
                Impuesto.IVA_19, "123456789"
            )
        );
        assertTrue(ex.getMessage().contains("precioVenta debe ser mayor a cero"));
    }

    @Test
    void transicionDescontinuadoAActivoLanzaExcepcion() {
        Producto producto = Producto.crear(
                productoId, empresaId, "SKU-001", "Nombre Prod", "Desc",
                categoriaId, UnidadMedida.UNIDAD, null, new BigDecimal("100.00"),
                Impuesto.IVA_19, "123456789"
        );
        
        producto.cambiarEstado(EstadoProducto.DESCONTINUADO);
        
        DomainException ex = assertThrows(DomainException.class, () -> 
            producto.cambiarEstado(EstadoProducto.ACTIVO)
        );
        assertTrue(ex.getMessage().contains("Un Producto DESCONTINUADO no puede volver a estado ACTIVO"));
    }
}
