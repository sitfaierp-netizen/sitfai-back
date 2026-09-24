package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.port;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.ClasificacionProducto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.AnalisisId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.ProductoId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida (Output Port): Contrato de persistencia para el Agregado {@link ClasificacionProducto}.
 * <p>
 * Implementado en la capa de Infraestructura por un adaptador JPA.
 * El Dominio NO conoce JPA, Hibernate ni ningún ORM (REGLA-1, REGLA-6).
 * <p>
 * Regla MT-01: Todas las firmas de método exigen {@link EmpresaId} para blindar
 * el aislamiento multi-tenant. Ninguna consulta puede ejecutarse sin el discriminador de tenant.
 */
public interface ClasificacionProductoRepository {

    /**
     * Persiste o actualiza una clasificación (Upsert semántico).
     *
     * @param clasificacion Agregado a guardar. Nunca null.
     */
    void guardar(ClasificacionProducto clasificacion);

    /**
     * Busca la clasificación activa de un producto en una bodega específica.
     * Regla MT-01: Siempre filtrado por empresaId.
     *
     * @param empresaId  Tenant del contexto de seguridad — nunca del payload HTTP.
     * @param bodegaId   Bodega de análisis.
     * @param productoId Producto a consultar.
     * @return           La clasificación si existe para ese tenant/bodega/producto.
     */
    Optional<ClasificacionProducto> buscarPorProducto(
            EmpresaId empresaId,
            BodegaId bodegaId,
            ProductoId productoId);

    /**
     * Busca una clasificación por su identidad propia.
     * Regla MT-01: Siempre filtrado por empresaId para garantizar aislamiento.
     *
     * @param empresaId Tenant del contexto de seguridad.
     * @param id        Identidad del Análisis.
     * @return          La clasificación si existe y pertenece al tenant.
     */
    Optional<ClasificacionProducto> buscarPorId(EmpresaId empresaId, AnalisisId id);

    /**
     * Obtiene todas las clasificaciones activas de una bodega.
     * Regla MT-01: Siempre filtrado por empresaId.
     * Útil para el cálculo batch del Análisis ABC completo de una bodega.
     *
     * @param empresaId Tenant del contexto de seguridad.
     * @param bodegaId  Bodega de análisis.
     * @return          Lista (posiblemente vacía) de clasificaciones.
     */
    List<ClasificacionProducto> listarPorBodega(EmpresaId empresaId, BodegaId bodegaId);
}
