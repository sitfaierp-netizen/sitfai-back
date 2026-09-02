package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida (Driven Port): Repositorio del Agregado Bodega.
 * <p>
 * Interface pura definida en el Dominio — sin imports de Spring Data, JPA ni ningún framework.
 * La implementación concreta ({@code BodegaJpaAdapter}) vive en la capa de Infraestructura.
 * <p>
 * Sigue el patrón Repository de DDD: trabaja con el Agregado completo, nunca con
 * sub-partes o entidades huérfanas.
 * <p>
 * Todos los métodos requieren {@code empresaId} para garantizar aislamiento de tenant (MT-01, MT-02).
 * <p>
 * Reglas validadas: REGLA-1 (Driven Port), REGLA-2 (ubicación en domain/port/output),
 * REGLA-3 (trabaja con Agregados), REGLA-4 (multitenancy por empresaId), MCP-01.
 */
public interface BodegaRepository {

    /**
     * Persiste un Agregado Bodega (INSERT o UPDATE).
     * También persiste los {@code MovimientoInventario} acumulados en el Agregado.
     *
     * @param bodega Agregado a persistir. No puede ser null.
     * @return El Agregado persistido (con posibles valores generados por la BD).
     */
    Bodega guardar(Bodega bodega);

    /**
     * Busca una Bodega por su ID dentro del tenant especificado.
     * <p>
     * El {@code empresaId} es obligatorio para garantizar que un tenant
     * no pueda acceder a Bodegas de otro tenant (MT-02).
     *
     * @param bodegaId  Identificador único de la Bodega.
     * @param empresaId Tenant del usuario que realiza la consulta.
     * @return Optional con la Bodega si existe y pertenece al tenant, vacío si no.
     */
    Optional<Bodega> buscarPorId(BodegaId bodegaId, EmpresaId empresaId);

    /**
     * Retorna todas las Bodegas activas de una Empresa.
     * Útil para listados operacionales.
     *
     * @param empresaId Tenant del que se quieren las Bodegas.
     * @return Lista (posiblemente vacía) de Bodegas activas del tenant.
     */
    List<Bodega> listarActivasPorEmpresa(EmpresaId empresaId);

    /**
     * Verifica si ya existe una Bodega con el mismo código dentro de la Sucursal
     * para el tenant dado (BOD-02: código único por Sucursal).
     * <p>
     * Usado por la capa de Aplicación antes de crear una nueva Bodega.
     *
     * @param empresaId   Tenant del contexto.
     * @param codigoBodega Código a verificar.
     * @return {@code true} si el código ya está en uso en la Sucursal.
     */
    boolean existeCodigoEnSucursal(EmpresaId empresaId, String sucursalId, String codigoBodega);

    /**
     * Verifica si ya existe una Bodega con el mismo código dentro de la Empresa.
     * @param empresaId Tenant del contexto
     * @param codigoBodega Código a verificar
     * @return true si existe
     */
    boolean existeCodigoEnEmpresa(EmpresaId empresaId, String codigoBodega);

    /**
     * Retorna todas las Bodegas de una Sucursal.
     *
     * @param empresaId Tenant del contexto.
     * @param sucursalId ID de la Sucursal.
     * @return Lista (posiblemente vacía) de Bodegas de la Sucursal.
     */
    List<Bodega> listarPorSucursal(EmpresaId empresaId, String sucursalId);

    /**
     * Retorna todas las Bodegas de una Sucursal sin empresaId.
     *
     * @param sucursalId ID de la Sucursal.
     * @return Lista (posiblemente vacia) de Bodegas de la Sucursal.
     */
    List<Bodega> listarPorSucursalId(String sucursalId);
}
