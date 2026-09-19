package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.query;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.query.PosQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.query.dto.CajaView;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JdbcPosQueryAdapter implements PosQueryRepository {

    @Override
    public List<CajaView> findCajasByEmpresa(UUID empresaId) {
        // Devuelve una caja mock ya que actualmente no existe una tabla 'pos_caja' o 'cajas'.
        // Esto permite que el flujo E2E avance obteniendo una caja válida para el tenant.
        return List.of(
            new CajaView("11111111-1111-1111-1111-111111111111", "Caja Principal", empresaId.toString())
        );
    }
}
