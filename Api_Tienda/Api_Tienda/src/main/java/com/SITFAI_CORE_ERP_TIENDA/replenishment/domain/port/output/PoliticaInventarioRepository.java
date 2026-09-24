package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;

import java.util.Optional;

/**
 * Puerto de Salida (Driven Port): Contrato de persistencia para PoliticaInventario.
 * Regla REGLA-1: Interfaz pura del dominio — sin dependencias de JPA ni Spring.
 * Regla MT-01: Toda consulta DEBE incluir EmpresaId para garantizar aislamiento de tenant.
 */
public interface PoliticaInventarioRepository {
    PoliticaInventario guardar(PoliticaInventario politica);
    Optional<PoliticaInventario> buscarPorId(PoliticaId id, EmpresaId empresaId);
    Optional<PoliticaInventario> buscarActivaPorBodegaYProducto(BodegaId bodegaId, ProductoId productoId, EmpresaId empresaId);
}
