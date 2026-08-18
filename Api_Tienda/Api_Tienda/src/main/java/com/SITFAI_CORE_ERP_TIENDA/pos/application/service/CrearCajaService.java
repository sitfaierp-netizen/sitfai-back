package com.SITFAI_CORE_ERP_TIENDA.pos.application.service;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CrearCajaCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.CrearCajaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CrearCajaService implements CrearCajaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearCajaService.class);

    @Override
    public UUID ejecutar(CrearCajaCommand command) {
        log.warn("Mock CrearCajaService: Caja creada mockeada para sucursalId={}", command.sucursalId());
        return UUID.randomUUID();
    }
}
