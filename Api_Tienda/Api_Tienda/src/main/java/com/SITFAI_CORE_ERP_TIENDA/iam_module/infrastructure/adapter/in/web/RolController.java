package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CrearRolCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RolResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.in.GestionarRolesUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.CrearRolRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iam/roles")
public class RolController {

    private final GestionarRolesUseCase gestionarRolesUseCase;

    public RolController(GestionarRolesUseCase gestionarRolesUseCase) {
        this.gestionarRolesUseCase = gestionarRolesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RolResponse>> listarRoles() {
        return ResponseEntity.ok(gestionarRolesUseCase.listarRoles());
    }

    @PostMapping
    public ResponseEntity<RolResponse> crearRol(@RequestBody CrearRolRequest request) {
        CrearRolCommand command = new CrearRolCommand(
                request.getCodigo(),
                request.getNombre(),
                request.getDescripcion(),
                request.getPermisos()
        );
        RolResponse response = gestionarRolesUseCase.crearRol(command);
        return ResponseEntity.ok(response);
    }
}
