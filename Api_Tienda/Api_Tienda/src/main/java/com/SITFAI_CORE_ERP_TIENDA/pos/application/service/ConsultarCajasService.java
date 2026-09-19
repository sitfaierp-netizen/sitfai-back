package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.ConsultarCajasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.query.PosQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.query.dto.CajaView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarCajasService implements ConsultarCajasUseCase {

    private final PosQueryRepository queryRepository;

    public ConsultarCajasService(PosQueryRepository queryRepository) {
        this.queryRepository = Objects.requireNonNull(queryRepository, "queryRepository no puede ser null");
    }

    @Override
    public List<CajaView> listarCajasPorEmpresa(UUID empresaId) {
        return queryRepository.findCajasByEmpresa(empresaId);
    }
}
