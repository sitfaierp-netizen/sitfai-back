package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.AutorizacionDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.LineaDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.CantidadDevuelta;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DevolucionId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EstadoRma;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.MotivoDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.port.output.AutorizacionDevolucionRepository;
import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence.entity.AutorizacionDevolucionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence.entity.LineaDevolucionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence.repository.SpringDataAutorizacionDevolucionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.lang.reflect.Field;

@Component
public class AutorizacionDevolucionJpaAdapter implements AutorizacionDevolucionRepository {

    private final SpringDataAutorizacionDevolucionRepository repository;

    public AutorizacionDevolucionJpaAdapter(SpringDataAutorizacionDevolucionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardar(EmpresaId empresaId, AutorizacionDevolucion autorizacion) {
        AutorizacionDevolucionJpaEntity entity = repository.findByIdAndEmpresaId(autorizacion.getDevolucionId().valor().toString(), empresaId.valor().toString())
                .orElseGet(() -> {
                    AutorizacionDevolucionJpaEntity newEntity = new AutorizacionDevolucionJpaEntity();
                    newEntity.setId(autorizacion.getDevolucionId().valor().toString());
                    newEntity.setEmpresaId(empresaId.valor().toString());
                    newEntity.setDocumentoFuenteId(autorizacion.getDocumentoFuenteId().valor().toString());
                    return newEntity;
                });
                
        entity.setEstado(autorizacion.getEstado());
        
        entity.getLineas().clear();
        for (LineaDevolucion linea : autorizacion.getLineas()) {
            LineaDevolucionJpaEntity lineaEntity = new LineaDevolucionJpaEntity();
            lineaEntity.setId(linea.getId().toString());
            lineaEntity.setProductoId(linea.getProductoId().valor().toString());
            lineaEntity.setCantidad(linea.getCantidadDevuelta().valor());
            lineaEntity.setMotivo(linea.getMotivoDevolucion().valor());
            lineaEntity.setEstadoInspeccion(linea.getEstadoInspeccion().name());
            lineaEntity.setAutorizacionDevolucion(entity);
            entity.getLineas().add(lineaEntity);
        }
        
        repository.save(entity);
    }

    @Override
    public Optional<AutorizacionDevolucion> buscarPorId(EmpresaId empresaId, DevolucionId devolucionId) {
        return repository.findByIdAndEmpresaId(devolucionId.valor().toString(), empresaId.valor().toString())
                .map(entity -> {
                    List<LineaDevolucion> lineas = entity.getLineas().stream()
                            .map(l -> {
                                LineaDevolucion linea = new LineaDevolucion(
                                        ProductoId.de(UUID.fromString(l.getProductoId())),
                                        CantidadDevuelta.de(l.getCantidad()),
                                        MotivoDevolucion.de(l.getMotivo())
                                );
                                // Rehydrate id and state via reflection to avoid mutating the domain randomly
                                try {
                                    Field idField = LineaDevolucion.class.getDeclaredField("id");
                                    idField.setAccessible(true);
                                    idField.set(linea, UUID.fromString(l.getId()));
                                    
                                    Field estadoField = LineaDevolucion.class.getDeclaredField("estadoInspeccion");
                                    estadoField.setAccessible(true);
                                    estadoField.set(linea, LineaDevolucion.EstadoInspeccion.valueOf(l.getEstadoInspeccion()));
                                } catch (Exception e) {
                                    throw new RuntimeException("Error rehydrating line", e);
                                }
                                return linea;
                            })
                            .collect(Collectors.toList());
                            
                    AutorizacionDevolucion rma = AutorizacionDevolucion.emitir(
                            EmpresaId.de(UUID.fromString(entity.getEmpresaId())),
                            DevolucionId.de(UUID.fromString(entity.getId())),
                            DocumentoFuenteId.de(UUID.fromString(entity.getDocumentoFuenteId())),
                            lineas
                    );
                    
                    try {
                        Field estadoField = AutorizacionDevolucion.class.getDeclaredField("estado");
                        estadoField.setAccessible(true);
                        estadoField.set(rma, entity.getEstado());
                    } catch (Exception e) {
                        throw new RuntimeException("Error rehydrating aggregate root state", e);
                    }
                    
                    return rma;
                });
    }
}
