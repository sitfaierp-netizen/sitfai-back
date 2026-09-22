package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.port;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;

import java.util.List;
import java.util.Optional;

/**
 * Output Port (Driven Port / SPI): Contrato de persistencia para el Agregado {@link Factura}.
 * <p>
 * Puro Java: Cero dependencias de JPA, Spring Data o bases de datos (REGLA-1, MCP-01).
 * Todos los métodos exigen obligatoriamente {@link EmpresaId} en su firma para garantizar
 * el aislamiento multitenant estricto (MT-01).
 */
public interface FacturaRepository {

    /**
     * Guarda o actualiza el Agregado {@link Factura} junto con todas sus líneas protegidas.
     *
     * @param factura Instancia del agregado a persistir.
     * @return Factura persistida.
     */
    Factura guardar(Factura factura);

    /**
     * Recupera una factura por su ID asegurando que pertenezca al Tenant indicado.
     *
     * @param id        Identificador de la Factura.
     * @param empresaId Identificador del Tenant (MT-01).
     * @return Optional con la factura si pertenece a la empresa, o Optional.empty() si no existe o pertenece a otro tenant.
     */
    Optional<Factura> buscarPorId(FacturaId id, EmpresaId empresaId);

    /**
     * Recupera todas las facturas asociadas a una empresa específica.
     *
     * @param empresaId Identificador del Tenant (MT-01).
     * @return Lista inmutable de facturas del tenant.
     */
    List<Factura> buscarPorEmpresa(EmpresaId empresaId);

    /**
     * Verifica la existencia de una factura para un tenant dado.
     *
     * @param id        Identificador de la Factura.
     * @param empresaId Identificador del Tenant (MT-01).
     * @return true si la factura existe para esa empresa, false en caso contrario.
     */
    boolean existePorId(FacturaId id, EmpresaId empresaId);
}
