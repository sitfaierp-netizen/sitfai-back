package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.ModuloSistema;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Rol;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out.RolRepositoryPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.PermisoModulo;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RolJpaAdapter implements RolRepositoryPort {

    private final RolJpaRepository repository;

    public RolJpaAdapter(RolJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Rol guardar(Rol rol) {
        RolJpaEntity entity = mapToEntity(rol);
        RolJpaEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Rol> buscarPorId(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<Rol> buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo).map(this::mapToDomain);
    }

    @Override
    public List<Rol> listarTodos() {
        return repository.findAll().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    private RolJpaEntity mapToEntity(Rol rol) {
        RolJpaEntity entity = new RolJpaEntity();
        entity.setId(rol.getId());
        entity.setCodigo(rol.getCodigo());
        entity.setNombre(rol.getNombre());
        entity.setDescripcion(rol.getDescripcion());
        
        List<RolPermisoEmbeddable> permisos = new ArrayList<>();
        if (rol.getPermisos() != null) {
            for (PermisoModulo pm : rol.getPermisos()) {
                if (pm.getAcciones() != null) {
                    for (String accion : pm.getAcciones()) {
                        permisos.add(new RolPermisoEmbeddable(pm.getModulo().name(), accion));
                    }
                }
            }
        }
        entity.setPermisos(permisos);
        return entity;
    }

    private Rol mapToDomain(RolJpaEntity entity) {
        Map<String, Set<String>> permisosMap = new HashMap<>();
        if (entity.getPermisos() != null) {
            for (RolPermisoEmbeddable embeddable : entity.getPermisos()) {
                permisosMap.computeIfAbsent(embeddable.getModulo(), k -> new HashSet<>()).add(embeddable.getAccion());
            }
        }
        
        List<PermisoModulo> permisos = permisosMap.entrySet().stream()
                .map(e -> new PermisoModulo(ModuloSistema.valueOf(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
        
        return new Rol(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion(),
                permisos
        );
    }
}
