package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.service;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CrearRolCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.PermisoModuloDto;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RolResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.in.GestionarRolesUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.RolCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Rol;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out.RolEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out.RolRepositoryPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.PermisoModulo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GestionarRolesService implements GestionarRolesUseCase {

    private final RolRepositoryPort rolRepositoryPort;
    private final RolEventPublisherPort rolEventPublisherPort;

    public GestionarRolesService(RolRepositoryPort rolRepositoryPort, RolEventPublisherPort rolEventPublisherPort) {
        this.rolRepositoryPort = rolRepositoryPort;
        this.rolEventPublisherPort = rolEventPublisherPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        return rolRepositoryPort.listarTodos().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RolResponse crearRol(CrearRolCommand command) {
        if (rolRepositoryPort.buscarPorCodigo(command.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("El rol con código " + command.getCodigo() + " ya existe");
        }

        List<PermisoModulo> permisos = command.getPermisos() != null 
                ? command.getPermisos().stream()
                    .map(dto -> new PermisoModulo(dto.getModulo(), dto.getAcciones()))
                    .collect(Collectors.toList()) 
                : List.of();

        Rol rol = new Rol(
                UUID.randomUUID().toString(),
                command.getCodigo(),
                command.getNombre(),
                command.getDescripcion(),
                permisos
        );

        Rol rolGuardado = rolRepositoryPort.guardar(rol);
        
        // Publicar evento para Keycloak Sync u otros
        rolEventPublisherPort.publicar(new RolCreadoEvent(
                rolGuardado.getId(),
                rolGuardado.getCodigo(),
                rolGuardado.getNombre()
        ));

        return mapToResponse(rolGuardado);
    }

    private RolResponse mapToResponse(Rol rol) {
        List<PermisoModuloDto> permisosDto = rol.getPermisos().stream()
                .map(p -> new PermisoModuloDto(p.getModulo(), p.getAcciones()))
                .collect(Collectors.toList());

        return new RolResponse(
                rol.getId(),
                rol.getCodigo(),
                rol.getNombre(),
                rol.getDescripcion(),
                permisosDto
        );
    }
}
