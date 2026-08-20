package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.proyector;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaActualizadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaEliminadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmpresaReadModelProyector {

    private final EmpresaJpaRepository empresaRepository;

    public EmpresaReadModelProyector(EmpresaJpaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @EventListener
    @Transactional
    public void on(EmpresaActualizadaEvent event) {
        empresaRepository.findById(event.empresaId().valor().toString())
                .ifPresent(entity -> {
                    entity.setRuc(event.ruc().valor());
                    entity.setRazonSocial(event.nombre().valor());
                    entity.setActualizadoEn(event.ocurridoEn());
                    empresaRepository.save(entity);
                });
    }

    @EventListener
    @Transactional
    public void on(EmpresaEliminadaEvent event) {
        empresaRepository.findById(event.empresaId().valor().toString())
                .ifPresent(entity -> {
                    entity.setEstado(EstadoEmpresa.ELIMINADO.name());
                    entity.setActualizadoEn(event.ocurridoEn());
                    empresaRepository.save(entity);
                });
    }
}
