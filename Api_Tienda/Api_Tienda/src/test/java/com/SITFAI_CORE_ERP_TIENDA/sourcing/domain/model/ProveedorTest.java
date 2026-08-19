package com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProveedorTest {

    private final ProveedorId proveedorId = ProveedorId.generar();
    private final EmpresaId empresaId = EmpresaId.de(UUID.randomUUID());
    private final Ruc ruc = new Ruc("1234567890001");

    @Test
    void debeCrearProveedorValido() {
        Proveedor proveedor = Proveedor.crear(
                proveedorId, empresaId, ruc, "Mi Proveedor", "correo@test.com",
                "555-1234", "Direccion", 5
        );
        
        assertNotNull(proveedor);
        assertEquals("Mi Proveedor", proveedor.getRazonSocial());
        assertEquals(5, proveedor.getPlazoEntregaDias());
        assertEquals(EstadoProveedor.ACTIVO, proveedor.getEstado());
    }

    @Test
    void plazoEntregaDiasNegativoLanzaExcepcion() {
        DomainException ex = assertThrows(DomainException.class, () -> 
            Proveedor.crear(
                proveedorId, empresaId, ruc, "Mi Proveedor", "correo@test.com",
                "555-1234", "Direccion", -1
            )
        );
        assertTrue(ex.getMessage().contains("plazo de entrega en días no puede ser negativo"));
    }
}
