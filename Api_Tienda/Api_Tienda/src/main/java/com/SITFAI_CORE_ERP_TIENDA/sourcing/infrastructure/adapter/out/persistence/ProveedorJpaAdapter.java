package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.ProveedorRepository;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.model.Proveedor;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.EstadoProveedor;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.entity.ProveedorJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.repository.ProveedorJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.exception.RucDuplicadoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProveedorJpaAdapter implements ProveedorRepository {

    private final ProveedorJpaRepository jpaRepository;

    public ProveedorJpaAdapter(ProveedorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void guardar(Proveedor proveedor) {
        try {
            ProveedorJpaEntity entity = toEntity(proveedor);
            jpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new RucDuplicadoException(proveedor.getRuc().valor(), proveedor.getEmpresaId().toString());
        }
    }

    @Override
    public Optional<Proveedor> buscarPorIdYEmpresa(ProveedorId id, EmpresaId empresaId) {
        return jpaRepository.findByIdAndEmpresaId(id.toString(), empresaId.toString())
                .map(this::toDomain);
    }

    private ProveedorJpaEntity toEntity(Proveedor p) {
        return new ProveedorJpaEntity(
                p.getProveedorId().toString(), p.getEmpresaId().toString(), p.getRuc().valor(),
                p.getRazonSocial(), p.getEmailContacto(), p.getTelefono(), p.getDireccion(),
                p.getPlazoEntregaDias(), p.getEstado().name(), p.getCreadoEn()
        );
    }

    private Proveedor toDomain(ProveedorJpaEntity e) {
        return Proveedor.reconstituir(
                ProveedorId.de(e.getId()), EmpresaId.de(UUID.fromString(e.getEmpresaId())),
                new Ruc(e.getRuc()), e.getRazonSocial(), e.getEmailContacto(), e.getTelefono(),
                e.getDireccion(), e.getPlazoEntregaDias(), EstadoProveedor.valueOf(e.getEstado()), e.getCreadoEn()
        );
    }
}
