package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.proyector;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalActualizadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalAgregadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalEliminadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoSucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.SucursalJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository.SucursalJpaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SucursalReadModelProyector {

    private final SucursalJpaRepository sucursalRepository;
    private final EmpresaJpaRepository empresaRepository;

    public SucursalReadModelProyector(SucursalJpaRepository sucursalRepository, EmpresaJpaRepository empresaRepository) {
        this.sucursalRepository = sucursalRepository;
        this.empresaRepository = empresaRepository;
    }

    @EventListener
    @Transactional
    public void on(SucursalAgregadaEvent event) {
        EmpresaJpaEntity empresa = empresaRepository.findById(event.empresaId().valor().toString())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada para el proyector"));

        SucursalJpaEntity entity = new SucursalJpaEntity(
                event.sucursalId().valor().toString(),
                empresa,
                event.codigoSucursal(),
                event.nombreSucursal(),
                EstadoSucursal.ACTIVA.name(),
                event.ocurridoEn(),
                event.ocurridoEn()
        );

        sucursalRepository.save(entity);
    }

    @EventListener
    @Transactional
    public void on(SucursalActualizadaEvent event) {
        sucursalRepository.findByIdAndEmpresa_Id(
                        event.sucursalId().valor().toString(),
                        event.empresaId().valor().toString())
                .ifPresent(entity -> {
                    entity.setCodigo(event.codigo());
                    entity.setNombre(event.nombre());
                    entity.setActualizadoEn(event.ocurridoEn());
                    sucursalRepository.save(entity);
                });
    }

    @EventListener
    @Transactional
    public void on(SucursalEliminadaEvent event) {
        sucursalRepository.findByIdAndEmpresa_Id(
                        event.sucursalId().valor().toString(),
                        event.empresaId().valor().toString())
                .ifPresent(entity -> {
                    entity.setEstado(EstadoSucursal.ELIMINADO.name());
                    entity.setActualizadoEn(event.ocurridoEn());
                    sucursalRepository.save(entity);
                });
    }
}
