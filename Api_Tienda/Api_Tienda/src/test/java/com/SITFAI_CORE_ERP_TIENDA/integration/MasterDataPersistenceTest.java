package com.SITFAI_CORE_ERP_TIENDA.integration;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import org.springframework.context.annotation.Import;

import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity.ProductoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.repository.ProductoJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.entity.ProveedorJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.entity.ProveedorJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.repository.ProveedorJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Docker is not available in the IDE environment")
class MasterDataPersistenceTest extends AbstractIntegrationTest {



    @Autowired
    private ProductoJpaRepository productoRepository;

    @Autowired
    private ProveedorJpaRepository proveedorRepository;

    @Test
    void debeGuardarProductoConPrecioCompraNulo() {
        String empresaId = UUID.randomUUID().toString();
        ProductoJpaEntity producto = new ProductoJpaEntity(
                UUID.randomUUID().toString(), empresaId, "SKU-001", "Prod Nulo", "Desc",
                UUID.randomUUID().toString(), "UNIDAD", null, new BigDecimal("100"),
                "IVA_19", "CODE", "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );

        ProductoJpaEntity saved = productoRepository.saveAndFlush(producto);
        assertNotNull(saved);
        assertNull(saved.getPrecioCompra());
    }

    @Test
    void debeRespetarUniqueConstraintProducto_EmpresaYsku() {
        String empresaId = UUID.randomUUID().toString();
        
        ProductoJpaEntity p1 = new ProductoJpaEntity(
                UUID.randomUUID().toString(), empresaId, "SKU-UNIQUE", "P1", "D",
                UUID.randomUUID().toString(), "UNIDAD", null, new BigDecimal("100"),
                "IVA_19", "C", "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );
        productoRepository.saveAndFlush(p1);

        ProductoJpaEntity p2 = new ProductoJpaEntity(
                UUID.randomUUID().toString(), empresaId, "SKU-UNIQUE", "P2", "D",
                UUID.randomUUID().toString(), "UNIDAD", null, new BigDecimal("100"),
                "IVA_19", "C", "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );

        assertThrows(DataIntegrityViolationException.class, () -> productoRepository.saveAndFlush(p2));
    }

    @Test
    void debeGuardarProveedorConPlazoEntrega() {
        String empresaId = UUID.randomUUID().toString();
        ProveedorJpaEntity proveedor = new ProveedorJpaEntity(
                UUID.randomUUID().toString(), empresaId, "123456789", "Razon", "correo@test.com",
                "123", "Dir", 15, "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );

        ProveedorJpaEntity saved = proveedorRepository.saveAndFlush(proveedor);
        assertNotNull(saved);
        assertEquals(15, saved.getPlazoEntregaDias());
    }

    @Test
    void debeRespetarUniqueConstraintProveedor_EmpresaYruc() {
        String empresaId = UUID.randomUUID().toString();
        
        ProveedorJpaEntity p1 = new ProveedorJpaEntity(
                UUID.randomUUID().toString(), empresaId, "RUC-UNIQUE", "P1", "correo@test.com",
                "123", "Dir", 10, "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );
        proveedorRepository.saveAndFlush(p1);

        ProveedorJpaEntity p2 = new ProveedorJpaEntity(
                UUID.randomUUID().toString(), empresaId, "RUC-UNIQUE", "P2", "correo@test.com",
                "123", "Dir", 10, "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );

        assertThrows(DataIntegrityViolationException.class, () -> proveedorRepository.saveAndFlush(p2));
    }
}
