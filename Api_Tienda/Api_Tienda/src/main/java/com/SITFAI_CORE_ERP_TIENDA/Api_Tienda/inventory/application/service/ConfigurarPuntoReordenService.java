package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConfigurarPuntoReordenCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ConfigurarPuntoReordenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.PuntoReorden;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio de Aplicación (Use Case) para configurar el punto de reorden (BOD-08).
 * <p>
 * Reglas validadas a nivel orquestación:
 * - MT-01: Aislamiento Multitenant (Búsqueda estricta por empresaId).
 * - REGLA-1: La lógica de dominio pertenece al Agregado, el servicio solo orquesta.
 */
@Service
public class ConfigurarPuntoReordenService implements ConfigurarPuntoReordenUseCase {

    private final BodegaRepository bodegaRepository;

    public ConfigurarPuntoReordenService(BodegaRepository bodegaRepository) {
        this.bodegaRepository = Objects.requireNonNull(bodegaRepository, "bodegaRepository no puede ser nulo");
    }

    @Override
    @Transactional
    public void ejecutar(ConfigurarPuntoReordenCommand command) {
        // 1. Instanciar Value Objects
        EmpresaId empresaId = EmpresaId.de(command.empresaId().toString());
        BodegaId bodegaId = BodegaId.de(command.bodegaId().toString());
        ProductoId productoId = ProductoId.de(command.productoId().toString());
        PuntoReorden puntoReorden = PuntoReorden.de(command.puntoReorden());

        // 2. Recuperar el Agregado respetando el tenant (MT-01)
        Bodega bodega = bodegaRepository.buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new BodegaNoEncontradaException(bodegaId, empresaId));

        // 3. Delegar la operación al Dominio
        bodega.establecerPuntoReorden(productoId, puntoReorden);

        // 4. Persistir los cambios
        bodegaRepository.guardar(bodega);
    }
}
