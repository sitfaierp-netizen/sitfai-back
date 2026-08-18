package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.LineaFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.FacturaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.LineaFacturaJpaEntity;

/**
 * Mapper de Persistencia: Aísla el Dominio de JPA.
 */
public final class FacturaPersistenceMapper {

    private FacturaPersistenceMapper() {}

    public static FacturaJpaEntity toEntity(FacturaElectronica domain) {
        FacturaJpaEntity entity = new FacturaJpaEntity();
        entity.setId(domain.getId().valor().toString());
        entity.setEmpresaId(domain.getEmpresaId().valor().toString());
        entity.setNitEmisor(domain.getNitEmisor().valor());
        entity.setNitReceptor(domain.getNitReceptor().valor());
        entity.setEstado(domain.getEstado().name());
        entity.setCufe(domain.getCufe() != null ? domain.getCufe().valor() : null);
        entity.setResolucionDian(domain.getResolucionDian().prefijo());
        
        entity.setSubtotal(domain.getSubtotal().monto());
        entity.setTotalImpuestos(domain.getTotalImpuestos().monto());
        entity.setTotalGeneral(domain.getTotalGeneral().monto());

        for (LineaFactura linea : domain.getLineas()) {
            LineaFacturaJpaEntity lineaEntity = new LineaFacturaJpaEntity();
            lineaEntity.setEmpresaId(domain.getEmpresaId().valor().toString());
            lineaEntity.setConcepto(linea.getConcepto());
            lineaEntity.setCantidad(linea.getCantidad());
            lineaEntity.setPrecioUnitario(linea.getPrecioUnitario().monto());
            lineaEntity.setSubtotal(linea.calcularSubtotal().monto());
            lineaEntity.setTotalImpuestos(linea.calcularTotalImpuestos().monto());
            entity.addLinea(lineaEntity);
        }

        return entity;
    }

    // toDomain se omitiría o implementaría si ConsultarFacturaUseCase devuelve Agregados, 
    // pero nuestra arquitectura devuelve DTOs (o si se re-hidrata para modificar).
    // Aquí implementado como esqueleto por completitud arquitectónica.
}
