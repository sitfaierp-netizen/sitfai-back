package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.CategoriaRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Categoria;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.CategoriaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EstadoCategoria;
import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity.CategoriaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.repository.CategoriaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoriaJpaAdapter implements CategoriaRepository {

    private final CategoriaJpaRepository repository;

    public CategoriaJpaAdapter(CategoriaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardar(Categoria categoria) {
        CategoriaJpaEntity entity = new CategoriaJpaEntity(
                categoria.getCategoriaId().valor().toString(),
                categoria.getEmpresaId().valor().toString(),
                categoria.getNombre(),
                categoria.getCategoriaPadreId() != null ? categoria.getCategoriaPadreId().valor().toString() : null,
                categoria.getEstado().name(),
                categoria.getCreadoEn()
        );
        repository.save(entity);
    }

    @Override
    public List<Categoria> obtenerTodasPorEmpresa(EmpresaId empresaId) {
        return repository.findByEmpresaId(empresaId.valor().toString()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Categoria toDomain(CategoriaJpaEntity entity) {
        return Categoria.reconstituir(
                CategoriaId.de(entity.getId()),
                EmpresaId.de(java.util.UUID.fromString(entity.getEmpresaId())),
                entity.getNombre(),
                entity.getCategoriaPadreId() != null ? CategoriaId.de(entity.getCategoriaPadreId()) : null,
                EstadoCategoria.valueOf(entity.getEstado()),
                entity.getCreadoEn()
        );
    }
}
