package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoSucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.SucursalJpaEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mapper bidireccional entre el Dominio de Core-Empresa y las entidades JPA (Regla 1 y Regla 5).
 */
public final class EmpresaPersistenceMapper {

    private EmpresaPersistenceMapper() {
        // Utility class
    }

    public static EmpresaJpaEntity toJpaEntity(Empresa domain) {
        if (domain == null) {
            return null;
        }

        EmpresaJpaEntity entity = new EmpresaJpaEntity();
        entity.setId(domain.getId().valor().toString());
        entity.setRuc(domain.getRuc().valor());
        entity.setRazonSocial(domain.getNombre().valor());
        entity.setEstado(domain.getEstado().name());
        entity.setCreadoEn(domain.getCreadoEn());
        entity.setActualizadoEn(domain.getActualizadoEn());
        entity.setVersion(domain.getVersion() != null ? domain.getVersion() : 0L);

        return entity;
    }

    public static Empresa toDomainEntity(EmpresaJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        List<Sucursal> sucursales = new ArrayList<>();

        return Empresa.reconstituir(
                EmpresaId.de(jpa.getId()),
                Ruc.de(jpa.getRuc()),
                NombreEmpresa.de(jpa.getRazonSocial()),
                EstadoEmpresa.valueOf(jpa.getEstado()),
                sucursales,
                jpa.getCreadoEn(),
                jpa.getActualizadoEn(),
                jpa.getVersion()
        );
    }

    public static List<Empresa> toDomainList(List<EmpresaJpaEntity> jpaList) {
        if (jpaList == null) {
            return Collections.emptyList();
        }
        return jpaList.stream().map(EmpresaPersistenceMapper::toDomainEntity).toList();
    }
}
