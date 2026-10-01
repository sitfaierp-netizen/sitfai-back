package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.service;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.GestionarPoliticaInventarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.PoliticaInventarioResult;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.exception.PoliticaInventarioNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.port.input.GestionarPoliticaInventarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.NivelOptimo;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PuntoReorden;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output.PoliticaInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class GestionarPoliticaInventarioService implements GestionarPoliticaInventarioUseCase {

    private final PoliticaInventarioRepository repository;

    public GestionarPoliticaInventarioService(PoliticaInventarioRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    @Transactional
    public PoliticaInventarioResult crear(GestionarPoliticaInventarioCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");
        PoliticaInventario politica = construir(UUID.randomUUID(), command);
        return toResult(repository.guardar(politica));
    }

    @Override
    @Transactional
    public PoliticaInventarioResult actualizar(GestionarPoliticaInventarioCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");
        if (command.politicaId() == null) {
            throw new IllegalArgumentException("PoliticaId es obligatorio para actualizar");
        }

        PoliticaId politicaId = new PoliticaId(command.politicaId());
        EmpresaId empresaId = new EmpresaId(command.empresaId());
        PoliticaInventario existente = repository.buscarPorId(politicaId, empresaId)
                .orElseThrow(() -> new PoliticaInventarioNoEncontradaException(command.politicaId()));

        PoliticaInventario actualizada = new PoliticaInventario(
                existente.getId(),
                existente.getEmpresaId(),
                new BodegaId(command.bodegaId()),
                new ProductoId(command.productoId()),
                new PuntoReorden(command.puntoReorden()),
                new NivelOptimo(command.nivelOptimo()),
                command.activa()
        );
        return toResult(repository.guardar(actualizada));
    }

    private PoliticaInventario construir(UUID id, GestionarPoliticaInventarioCommand command) {
        return new PoliticaInventario(
                new PoliticaId(id),
                new EmpresaId(command.empresaId()),
                new BodegaId(command.bodegaId()),
                new ProductoId(command.productoId()),
                new PuntoReorden(command.puntoReorden()),
                new NivelOptimo(command.nivelOptimo()),
                command.activa()
        );
    }

    private PoliticaInventarioResult toResult(PoliticaInventario politica) {
        return new PoliticaInventarioResult(
                politica.getId().valor(),
                politica.getEmpresaId().valor(),
                politica.getBodegaId().valor(),
                politica.getProductoId().valor(),
                politica.getPuntoReorden().valor(),
                politica.getNivelOptimo().valor(),
                politica.isActiva()
        );
    }

}
