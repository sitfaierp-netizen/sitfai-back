package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.mapper.EmpresaApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.ConsultarEmpresaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Consultas y lecturas del Bounded Context core-empresa.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarEmpresaService implements ConsultarEmpresaUseCase {

    private final EmpresaRepository empresaRepository;

    public ConsultarEmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = Objects.requireNonNull(empresaRepository, "empresaRepository no puede ser null.");
    }

    @Override
    public EmpresaResponse obtenerPorId(UUID empresaId) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser null.");

        EmpresaId id = EmpresaId.de(empresaId);
        Empresa empresa = empresaRepository.buscarPorId(id)
                .orElseThrow(() -> new EmpresaNoEncontradaException(id));

        return EmpresaApplicationMapper.toResponse(empresa);
    }

    @Override
    public EmpresaResponse obtenerPorRuc(String rucStr) {
        Objects.requireNonNull(rucStr, "RUC no puede ser null.");

        Ruc ruc = Ruc.de(rucStr);
        Empresa empresa = empresaRepository.buscarPorRuc(ruc)
                .orElseThrow(() -> new EmpresaNoEncontradaException(
                        "No se encontró ninguna empresa con el RUC " + ruc.valor()
                ));

        return EmpresaApplicationMapper.toResponse(empresa);
    }

    @Override
    public List<EmpresaResponse> listarTodas() {
        return EmpresaApplicationMapper.toResponseList(empresaRepository.buscarTodas());
    }
}
