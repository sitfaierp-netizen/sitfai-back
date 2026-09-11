package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CategoriaResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearCategoriaCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.GestionarCategoriasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.CategoriaRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Categoria;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.CategoriaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class GestionarCategoriasService implements GestionarCategoriasUseCase {

    private final CategoriaRepository categoriaRepository;

    public GestionarCategoriasService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = Objects.requireNonNull(categoriaRepository);
    }

    @Override
    public CategoriaResponse crear(CrearCategoriaCommand command, UUID empresaId) {
        EmpresaId eId = EmpresaId.de(empresaId);
        CategoriaId padreId = command.categoriaPadreId() != null && !command.categoriaPadreId().isBlank()
                ? CategoriaId.de(command.categoriaPadreId())
                : null;

        Categoria categoria = Categoria.crear(
                CategoriaId.generar(),
                eId,
                command.nombre(),
                padreId
        );

        categoriaRepository.guardar(categoria);

        return toResponse(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarPorEmpresa(UUID empresaId) {
        EmpresaId eId = EmpresaId.de(empresaId);
        return categoriaRepository.obtenerTodasPorEmpresa(eId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(
                c.getCategoriaId().valor().toString(),
                c.getEmpresaId().valor().toString(),
                c.getNombre(),
                c.getCategoriaPadreId() != null ? c.getCategoriaPadreId().valor().toString() : null,
                c.getEstado().name(),
                c.getCreadoEn()
        );
    }
}
