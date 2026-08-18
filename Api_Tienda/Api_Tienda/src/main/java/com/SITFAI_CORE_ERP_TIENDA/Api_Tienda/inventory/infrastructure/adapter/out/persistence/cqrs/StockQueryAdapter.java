package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.cqrs;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.StockQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.ConsultarStockConsolidadoQuery;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.StockConsolidadoView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockQueryAdapter implements StockQueryRepository {

    private final StockProyeccionJpaRepository repository;

    public StockQueryAdapter(StockProyeccionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StockConsolidadoView> consultarStockConsolidado(ConsultarStockConsolidadoQuery query) {
        List<StockProyeccionJpaEntity> entities = repository.findByEmpresaIdAndBodegaId(
                query.empresaId(),
                query.bodegaId()
        );

        return entities.stream()
                .map(e -> new StockConsolidadoView(
                        e.getEmpresaId(),
                        e.getBodegaId(),
                        e.getProductoId(),
                        e.getCantidadTotal(),
                        e.getUltimaActualizacion()
                ))
                .collect(Collectors.toList());
    }
}
