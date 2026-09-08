package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.*;
import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity.ProductoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.repository.ProductoJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.exception.SkuDuplicadoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven Adapter: Implementa ProductoRepository usando Spring Data JPA.
 * Traduce excepciones de infraestructura (SQL) a excepciones de negocio.
 */
@Repository
public class ProductoJpaAdapter implements ProductoRepository {

    private final ProductoJpaRepository jpaRepository;

    public ProductoJpaAdapter(ProductoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void guardar(Producto producto) {
        try {
            ProductoJpaEntity entity = toEntity(producto);
            jpaRepository.saveAndFlush(entity); // Flush inmediato para capturar DataIntegrityViolationException
        } catch (DataIntegrityViolationException e) {
            // Asumimos que la única restricción UNIQUE es empresa_id + sku (declarada en DB y JPA)
            throw new SkuDuplicadoException(producto.getSku(), producto.getEmpresaId().toString());
        }
    }

    @Override
    public Optional<Producto> buscarPorIdYEmpresa(ProductoId id, EmpresaId empresaId) {
        return jpaRepository.findByIdAndEmpresaId(id.toString(), empresaId.toString())
                .map(this::toDomain);
    }

    @Override
    public org.springframework.data.domain.Page<Producto> listarPorEmpresa(EmpresaId empresaId, String search, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<ProductoJpaEntity> resultPage;
        if (search == null || search.trim().isEmpty()) {
            resultPage = jpaRepository.findAllByEmpresaId(empresaId.toString(), pageable);
        } else {
            resultPage = jpaRepository.searchByEmpresaIdAndKeyword(empresaId.toString(), search.trim(), pageable);
        }
        return resultPage.map(this::toDomain);
    }

    @Override
    public boolean existeCodigoBarras(String codigoBarras, EmpresaId empresaId, ProductoId excluyendoProductoId) {
        return jpaRepository.existsByCodigoBarrasAndEmpresaIdAndIdNot(codigoBarras, empresaId.toString(), excluyendoProductoId.toString());
    }

    @Override
    public void deleteById(ProductoId id) {
        jpaRepository.deleteById(id.toString());
        jpaRepository.flush(); // To trigger DataIntegrityViolationException immediately
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------
    private ProductoJpaEntity toEntity(Producto p) {
        return new ProductoJpaEntity(
                p.getProductoId().toString(), p.getEmpresaId().toString(), p.getSku(),
                p.getNombre(), p.getDescripcion(), p.getCategoriaId().toString(),
                p.getUnidadMedida().name(), p.getPrecioCompra(), p.getPrecioVenta(),
                p.getImpuesto().name(), p.getCodigoBarras(), p.getEstado().name(),
                p.getCreadoEn(), p.getActualizadoEn(), p.isActivo(),
                p.getDeletedAt(), p.getDeletedBy()
        );
    }

    private Producto toDomain(ProductoJpaEntity e) {
        return Producto.reconstituir(
                ProductoId.de(e.getId()), EmpresaId.de(UUID.fromString(e.getEmpresaId())),
                e.getSku(), e.getNombre(), e.getDescripcion(),
                CategoriaId.de(e.getCategoriaId()), UnidadMedida.valueOf(e.getUnidadMedida()),
                e.getPrecioCompra(), e.getPrecioVenta(), Impuesto.valueOf(e.getImpuesto()),
                e.getCodigoBarras(), EstadoProducto.valueOf(e.getEstado()),
                e.getCreadoEn(), e.getActualizadoEn(), e.isActivo(),
                e.getDeletedAt(), e.getDeletedBy()
        );
    }
}
