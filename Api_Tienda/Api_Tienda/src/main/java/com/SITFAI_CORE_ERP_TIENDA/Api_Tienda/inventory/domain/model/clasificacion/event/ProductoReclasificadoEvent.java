package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.AnalisisId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.CategoriaABC;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.ProductoId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: ProductoReclasificadoEvent.
 * <p>
 * Se emite cuando el Agregado {@link com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.ClasificacionProducto}
 * detecta que la categoría ABC de un producto ha cambiado tras una reevaluación.
 * <p>
 * El evento es inmutable (record Java 21). Contiene únicamente datos primitivos y VOs
 * del propio dominio — nunca referencias a servicios ni repositorios (REGLA-3).
 * <p>
 * Uso downstream:
 * <ul>
 *   <li>El módulo {@code replenishment} puede ajustar el Punto de Reorden al detectar
 *       un cambio de categoría B→A (mayor criticidad).</li>
 *   <li>El módulo de auditoría puede registrar el cambio en el Event Store.</li>
 * </ul>
 * Regla MT-01: El {@code empresaId} asegura el aislamiento de tenant en consumidores.
 * Regla AUD-01: Contiene {@code ocurridoEn} como timestamp de auditoría.
 */
public record ProductoReclasificadoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        BodegaId bodegaId,
        ProductoId productoId,
        AnalisisId clasificacionId,
        CategoriaABC categoriaAnterior,
        CategoriaABC categoriaNueva
) {
    public ProductoReclasificadoEvent {
        Objects.requireNonNull(empresaId,        "ProductoReclasificadoEvent: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(bodegaId,         "ProductoReclasificadoEvent: bodegaId es obligatorio.");
        Objects.requireNonNull(productoId,       "ProductoReclasificadoEvent: productoId es obligatorio.");
        Objects.requireNonNull(clasificacionId,  "ProductoReclasificadoEvent: clasificacionId es obligatorio.");
        Objects.requireNonNull(categoriaAnterior,"ProductoReclasificadoEvent: categoriaAnterior es obligatoria.");
        Objects.requireNonNull(categoriaNueva,   "ProductoReclasificadoEvent: categoriaNueva es obligatoria.");
        if (categoriaAnterior == categoriaNueva) {
            throw new IllegalArgumentException(
                    "ProductoReclasificadoEvent: categoriaAnterior y categoriaNueva no pueden ser iguales. " +
                    "El evento solo se emite ante un cambio real de categoría.");
        }
        // Valores de sistema que se auto-asignan si no vienen del emisor
        if (eventoId == null)    eventoId    = UUID.randomUUID();
        if (ocurridoEn == null)  ocurridoEn  = Instant.now();
    }

    /**
     * Factory method de conveniencia para construir el evento completo.
     */
    public static ProductoReclasificadoEvent of(
            EmpresaId empresaId,
            BodegaId bodegaId,
            ProductoId productoId,
            AnalisisId clasificacionId,
            CategoriaABC categoriaAnterior,
            CategoriaABC categoriaNueva) {
        return new ProductoReclasificadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                bodegaId,
                productoId,
                clasificacionId,
                categoriaAnterior,
                categoriaNueva
        );
    }
}
