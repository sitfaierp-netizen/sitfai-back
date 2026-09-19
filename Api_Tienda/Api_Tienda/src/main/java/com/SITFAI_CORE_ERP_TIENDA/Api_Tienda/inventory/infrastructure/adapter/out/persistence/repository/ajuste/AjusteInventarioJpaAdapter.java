package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.ajuste;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.AjusteInventario;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.AjusteInventarioId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.AjusteInventarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.jpa.entity.ajuste.AjusteInventarioJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.mapper.ajuste.AjusteInventarioJpaMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AjusteInventarioJpaAdapter implements AjusteInventarioRepository {

    private final AjusteInventarioJpaRepository jpaRepository;
    private final AjusteInventarioJpaMapper mapper;

    public AjusteInventarioJpaAdapter(AjusteInventarioJpaRepository jpaRepository, AjusteInventarioJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(AjusteInventario ajuste) {
        if (ajuste == null) return;
        AjusteInventarioJpaEntity entity = mapper.toEntity(ajuste);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<AjusteInventario> buscarPorIdYEmpresaId(AjusteInventarioId id, EmpresaId empresaId) {
        return jpaRepository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(mapper::toDomain);
    }
}
