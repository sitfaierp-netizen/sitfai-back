package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CrearBodegaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.mapper.InventarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.CrearBodegaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * APPLICATION SERVICE: Crear Bodega
 * ═══════════════════════════════════════════════════════════════════════════════
 * <p>
 * Orquesta el caso de uso de creación de una Bodega. No contiene lógica de
 * negocio — solo coordina el Dominio y los puertos de salida.
 * <p>
 * ── FLUJO ────────────────────────────────────────────────────────────────────
 * 1. Valida unicidad del código en la Sucursal (BOD-02) → si duplicado, falla rápido.
 * 2. Construye los Value Objects desde el Command.
 * 3. Invoca el factory method del Dominio: {@code Bodega.crear(...)}.
 * 4. Persiste el Agregado vía {@code BodegaRepository.guardar(bodega)}.
 * 5. Drena y publica los Domain Events (si aplica — aquí Bodega.crear no emite eventos).
 * 6. Retorna el {@code BodegaResponse} mapeado.
 * <p>
 * ── RESTRICCIONES ────────────────────────────────────────────────────────────
 * - Usa {@code @Service} y {@code @Transactional} de Spring (válido en Application Layer).
 * - No contiene SQL, JPA, ni lógica HTTP.
 * - Llama al Dominio a través de factory methods y puertos — jamás accede a repositorios
 *   directamente con queries propias.
 * <p>
 * Reglas validadas: REGLA-1 (Application Layer), REGLA-2, BOD-01, BOD-02, MT-01.
 */
@Service
public class CrearBodegaService implements CrearBodegaUseCase {

    private final BodegaRepository bodegaRepository;

    public CrearBodegaService(BodegaRepository bodegaRepository) {
        this.bodegaRepository = bodegaRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException si ya existe una Bodega con el mismo código en la Sucursal (BOD-02).
     */
    @Override
    @Transactional
    public BodegaResponse ejecutar(CrearBodegaCommand command) {

        // ── 1. Construir Value Objects desde el Command ────────────────────────
        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        SucursalId sucursalId = SucursalId.de(command.sucursalId());

        // ── 2. Validar unicidad del código (BOD-02) ───────────────────────────
        boolean codigoYaExiste = bodegaRepository.existeCodigoEnSucursal(
                empresaId,
                command.sucursalId(),
                command.codigo()
        );
        if (codigoYaExiste) {
            throw new IllegalArgumentException(
                    String.format("[BOD-02] Ya existe una Bodega con el código '%s' en la Sucursal '%s' " +
                                    "para la Empresa '%s'.",
                            command.codigo(), command.sucursalId(), command.empresaId())
            );
        }

        // ── 3. Instanciar el Agregado vía factory method del Dominio ──────────
        Bodega bodega = Bodega.crear(
                empresaId,
                sucursalId,
                command.codigo(),
                command.nombre()
        );

        // ── 4. Persistir el Agregado ──────────────────────────────────────────
        Bodega bodegaPersistida = bodegaRepository.guardar(bodega);

        // ── 5. Drenar Domain Events (Bodega.crear no emite eventos; preparado
        //       para cuando se agreguen eventos de auditoría de creación) ───────
        bodegaPersistida.drainDomainEvents();
        // TODO: publicar eventos cuando se implemente el EventPublisher (Infraestructura)

        // ── 6. Mapear y retornar respuesta ────────────────────────────────────
        return InventarioApplicationMapper.toBodegaResponse(bodegaPersistida);
    }
}
