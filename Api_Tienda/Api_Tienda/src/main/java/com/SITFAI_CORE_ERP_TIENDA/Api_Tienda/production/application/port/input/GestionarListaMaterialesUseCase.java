package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.AgregarComponenteCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.AprobarRecetaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.CrearBorradorRecetaCommand;

import java.util.UUID;

public interface GestionarListaMaterialesUseCase {
    UUID crearBorradorReceta(CrearBorradorRecetaCommand command);
    void agregarComponente(AgregarComponenteCommand command);
    void aprobarReceta(AprobarRecetaCommand command);
}
