package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.identity;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception.IdentityConflictException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception.IdentityProvisioningException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.IdentityProvisioningPort;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.RolUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador idempotente de provisioning. No crea ni transporta contraseñas:
 * las identidades nuevas quedan obligadas a completar UPDATE_PASSWORD.
 */
@Component
public class KeycloakIdentityProvisioningAdapter implements IdentityProvisioningPort {

    static final String ATTR_EMPRESA_ID = "empresa_id";
    static final String ATTR_USUARIO_ID = "sitfai_usuario_id";
    static final String REQUIRED_ACTION_UPDATE_PASSWORD = "UPDATE_PASSWORD";

    private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityProvisioningAdapter.class);
    private static final Set<String> MANAGED_ROLES = Arrays.stream(RolUsuario.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private final Keycloak keycloak;
    private final String targetRealm;

    public KeycloakIdentityProvisioningAdapter(
            Keycloak keycloak,
            @Value("${keycloak.admin.realm:sitfai-erp}") String targetRealm
    ) {
        this.keycloak = Objects.requireNonNull(keycloak, "keycloak client no puede ser null.");
        this.targetRealm = requireText(targetRealm, "targetRealm no puede ser vacío.");
    }

    @Override
    public void provisionar(Usuario usuario) {
        ejecutarSeguro("provisionar", usuario, () -> {
            RealmResource realm = keycloak.realm(targetRealm);
            UsersResource users = realm.users();
            UserRepresentation identity = buscarExacto(users, usuario.getUsername().valor());
            boolean creada = false;

            if (identity == null) {
                identity = nuevaIdentidad(usuario);
                String createdId = crear(users, identity);
                identity = users.get(createdId).toRepresentation();
                creada = true;
            } else {
                validarPropiedad(identity, usuario);
            }

            UserResource userResource = users.get(identity.getId());
            actualizarPerfil(userResource, identity, usuario, creada);
            sincronizarRol(realm, userResource, usuario.getRol().name());
            log.info("Identidad Keycloak preparada: usuarioId={}, empresaId={}",
                    usuario.getId().valor(), usuario.getEmpresaId().valor());
        });
    }

    @Override
    public void completarOnboarding(Usuario usuario) {
        ejecutarSeguro("completar onboarding", usuario, () -> {
            if (!usuario.estaActivo()) {
                throw new IdentityProvisioningException("El usuario local aún no está activo.");
            }
            UserRef ref = usuarioObligatorio(keycloak.realm(targetRealm).users(), usuario);
            ref.representation().setEnabled(true);
            ref.resource().update(ref.representation());
            enviarOnboardingSiPendiente(ref.resource(), ref.representation());
            log.info("Onboarding Keycloak habilitado: usuarioId={}, empresaId={}",
                    usuario.getId().valor(), usuario.getEmpresaId().valor());
        });
    }

    @Override
    public void sincronizarRol(Usuario usuario) {
        ejecutarSeguro("sincronizar rol", usuario, () -> {
            RealmResource realm = keycloak.realm(targetRealm);
            UserResource user = usuarioObligatorio(realm.users(), usuario).resource();
            sincronizarRol(realm, user, usuario.getRol().name());
        });
    }

    @Override
    public void sincronizarEstado(Usuario usuario) {
        ejecutarSeguro("sincronizar estado", usuario, () -> {
            UserRef ref = usuarioObligatorio(keycloak.realm(targetRealm).users(), usuario);
            ref.representation().setEnabled(usuario.estaActivo());
            ref.resource().update(ref.representation());
        });
    }

    @Override
    public void reconciliar(Usuario usuario) {
        provisionar(usuario);
        if (usuario.estaActivo()) {
            completarOnboarding(usuario);
        }
    }

    private UserRepresentation nuevaIdentidad(Usuario usuario) {
        UserRepresentation identity = new UserRepresentation();
        identity.setUsername(usuario.getUsername().valor());
        identity.setEmail(usuario.getEmail().valor());
        identity.setEmailVerified(false);
        identity.setEnabled(usuario.estaActivo());
        identity.setRequiredActions(List.of(REQUIRED_ACTION_UPDATE_PASSWORD));
        identity.setAttributes(atributos(usuario));
        return identity;
    }

    private String crear(UsersResource users, UserRepresentation identity) {
        try (Response response = users.create(identity)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                return CreatedResponseUtil.getCreatedId(response);
            }
            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                UserRepresentation concurrente = buscarExacto(users, identity.getUsername());
                if (concurrente != null) {
                    return concurrente.getId();
                }
                throw new IdentityConflictException("La identidad externa ya existe y no pudo reconciliarse.");
            }
            throw new IdentityProvisioningException(
                    "Keycloak rechazó el provisioning con estado HTTP " + response.getStatus() + ".");
        }
    }

    private void actualizarPerfil(
            UserResource resource,
            UserRepresentation identity,
            Usuario usuario,
            boolean creada
    ) {
        validarPropiedad(identity, usuario);
        identity.setEmail(usuario.getEmail().valor());
        identity.setEnabled(usuario.estaActivo());
        identity.setAttributes(atributos(usuario));
        if (creada) {
            identity.setEmailVerified(false);
            identity.setRequiredActions(List.of(REQUIRED_ACTION_UPDATE_PASSWORD));
        }
        resource.update(identity);
    }

    private void enviarOnboardingSiPendiente(UserResource resource, UserRepresentation identity) {
        List<String> requiredActions = identity.getRequiredActions();
        if (requiredActions != null && requiredActions.contains(REQUIRED_ACTION_UPDATE_PASSWORD)) {
            resource.executeActionsEmail(List.of(REQUIRED_ACTION_UPDATE_PASSWORD));
        }
    }

    private Map<String, List<String>> atributos(Usuario usuario) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ATTR_EMPRESA_ID, List.of(usuario.getEmpresaId().valor().toString()));
        attributes.put(ATTR_USUARIO_ID, List.of(usuario.getId().valor().toString()));
        return attributes;
    }

    private void validarPropiedad(UserRepresentation identity, Usuario usuario) {
        Map<String, List<String>> attributes = identity.getAttributes();
        String empresaId = primerAtributo(attributes, ATTR_EMPRESA_ID);
        String usuarioId = primerAtributo(attributes, ATTR_USUARIO_ID);
        String esperadoEmpresa = usuario.getEmpresaId().valor().toString();
        String esperadoUsuario = usuario.getId().valor().toString();

        if (!esperadoEmpresa.equals(empresaId)) {
            throw new IdentityConflictException("El username pertenece a otro tenant o carece de tenant verificable.");
        }
        if (usuarioId != null && !esperadoUsuario.equals(usuarioId)) {
            throw new IdentityConflictException("La identidad externa pertenece a otro usuario local.");
        }
        if (identity.getEmail() != null && !identity.getEmail().isBlank()
                && !usuario.getEmail().valor().equalsIgnoreCase(identity.getEmail())) {
            throw new IdentityConflictException("La identidad externa tiene un correo diferente.");
        }
    }

    private String primerAtributo(Map<String, List<String>> attributes, String name) {
        if (attributes == null) {
            return null;
        }
        List<String> values = attributes.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private void sincronizarRol(RealmResource realm, UserResource user, String roleName) {
        RoleRepresentation target;
        try {
            target = realm.roles().get(roleName).toRepresentation();
        } catch (WebApplicationException ex) {
            throw new IdentityProvisioningException("El rol requerido no existe en Keycloak.", ex);
        }

        RoleScopeResource roles = user.roles().realmLevel();
        List<RoleRepresentation> managedAssigned = new ArrayList<>(roles.listAll().stream()
                .filter(role -> MANAGED_ROLES.contains(role.getName()))
                .toList());
        if (!managedAssigned.isEmpty()) {
            roles.remove(managedAssigned);
        }
        roles.add(List.of(target));
    }

    private UserRef usuarioObligatorio(UsersResource users, Usuario usuario) {
        UserRepresentation representation = buscarExacto(users, usuario.getUsername().valor());
        if (representation == null) {
            throw new IdentityConflictException("La identidad externa no existe; ejecute reconciliación.");
        }
        validarPropiedad(representation, usuario);
        return new UserRef(users.get(representation.getId()), representation);
    }

    private UserRepresentation buscarExacto(UsersResource users, String username) {
        return users.searchByUsername(username, true).stream()
                .filter(user -> username.equalsIgnoreCase(user.getUsername()))
                .findFirst()
                .orElse(null);
    }

    private void ejecutarSeguro(String operation, Usuario usuario, Runnable action) {
        Objects.requireNonNull(usuario, "usuario no puede ser null.");
        try {
            action.run();
        } catch (IdentityConflictException | IdentityProvisioningException ex) {
            throw ex;
        } catch (ProcessingException ex) {
            log.warn("Keycloak no disponible durante {}: usuarioId={}, empresaId={}",
                    operation, usuario.getId().valor(), usuario.getEmpresaId().valor());
            throw new IdentityProvisioningException("Keycloak no está disponible.", ex);
        } catch (WebApplicationException ex) {
            log.warn("Keycloak rechazó {}: usuarioId={}, empresaId={}, status={}",
                    operation, usuario.getId().valor(), usuario.getEmpresaId().valor(), ex.getResponse().getStatus());
            throw new IdentityProvisioningException("Keycloak rechazó la operación.", ex);
        } catch (RuntimeException ex) {
            log.warn("Fallo de Keycloak durante {}: usuarioId={}, empresaId={}",
                    operation, usuario.getId().valor(), usuario.getEmpresaId().valor());
            throw new IdentityProvisioningException("Falló la operación con Keycloak.", ex);
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private record UserRef(UserResource resource, UserRepresentation representation) {
    }
}
