package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.ResolucionRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.ResolucionDian;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.ResolucionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository.ResolucionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResolucionJpaAdapter implements ResolucionRepository {

    private final ResolucionJpaRepository repository;

    public ResolucionJpaAdapter(ResolucionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ResolucionDian> obtenerActiva(EmpresaId empresaId) {
        return repository.findByEmpresaIdAndActivaTrue(empresaId.valor().toString())
                .map(entity -> new ResolucionDian(
                        entity.getPrefijo(),
                        entity.getRangoInicial(),
                        entity.getRangoFinal(),
                        entity.getVigenciaHasta()
                ));
    }
}
