package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoSucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Tests para {@link CambiarEstadoSucursalService} (Regla 8 — Application Layer: JUnit5 + Mockito).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CambiarEstadoSucursalService — Tests de la capa de Aplicación")
class CambiarEstadoSucursalServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    private CambiarEstadoSucursalService sut;

    private UUID empresaIdUuid;
    private UUID sucursalIdUuid;
    private EmpresaId empresaId;
    private SucursalId sucursalId;

    @BeforeEach
    void setUp() {
        sut = new CambiarEstadoSucursalService(empresaRepository, sucursalRepository);
        empresaIdUuid = UUID.randomUUID();
        sucursalIdUuid = UUID.randomUUID();
        empresaId = EmpresaId.de(empresaIdUuid);
        sucursalId = SucursalId.de(sucursalIdUuid);
    }

    private Empresa unaEmpresaActiva() {
        return Empresa.reconstituir(
                empresaId,
                new Ruc("12345678901"),
                new NombreEmpresa("Empresa Test S.A."),
                EstadoEmpresa.ACTIVA,
                Collections.emptyList(),
                Instant.now(),
                Instant.now(),
                0L,
                true,
                null,
                null
        );
    }

    private Empresa unaEmpresaSuspendida() {
        return Empresa.reconstituir(
                empresaId,
                new Ruc("12345678901"),
                new NombreEmpresa("Empresa Test S.A."),
                EstadoEmpresa.SUSPENDIDA,
                Collections.emptyList(),
                Instant.now(),
                Instant.now(),
                0L,
                true,
                null,
                null
        );
    }

    private Sucursal unaSucursalActiva() {
        return Sucursal.reconstituir(
                sucursalId, "MATRIZ", "Sucursal Matriz",
                EstadoSucursal.ACTIVA, Instant.now(), Instant.now(), true, null, null
        );
    }

    private Sucursal unaSucursalInactiva() {
        return Sucursal.reconstituir(
                sucursalId, "MATRIZ", "Sucursal Matriz",
                EstadoSucursal.INACTIVA, Instant.now(), Instant.now(), true, null, null
        );
    }

    @Test
    @DisplayName("ACTIVA → INACTIVA: debe desactivar la sucursal y retornar INACTIVA")
    void ejecutar_sucursalActiva_laDesactiva() {
        // GIVEN
        Sucursal sucursalActiva = unaSucursalActiva();
        Sucursal sucursalInactivada = unaSucursalInactiva(); // Lo que el adapter retornaría

        when(empresaRepository.buscarPorId(empresaId)).thenReturn(Optional.of(unaEmpresaActiva()));
        when(sucursalRepository.buscarPorIdYEmpresaId(sucursalId, empresaId)).thenReturn(Optional.of(sucursalActiva));
        when(sucursalRepository.guardar(any(), any())).thenReturn(sucursalInactivada);

        // WHEN
        SucursalResponse result = sut.ejecutar(empresaIdUuid, sucursalIdUuid);

        // THEN
        assertThat(result.estado()).isEqualTo("INACTIVA");
        verify(sucursalRepository).guardar(sucursalActiva, empresaId);
    }

    @Test
    @DisplayName("INACTIVA → ACTIVA: debe activar la sucursal cuando la Empresa está ACTIVA")
    void ejecutar_sucursalInactiva_conEmpresaActiva_laActiva() {
        // GIVEN
        Sucursal sucursalInactiva = unaSucursalInactiva();
        Sucursal sucursalActivada = unaSucursalActiva();

        when(empresaRepository.buscarPorId(empresaId)).thenReturn(Optional.of(unaEmpresaActiva()));
        when(sucursalRepository.buscarPorIdYEmpresaId(sucursalId, empresaId)).thenReturn(Optional.of(sucursalInactiva));
        when(sucursalRepository.guardar(any(), any())).thenReturn(sucursalActivada);

        // WHEN
        SucursalResponse result = sut.ejecutar(empresaIdUuid, sucursalIdUuid);

        // THEN
        assertThat(result.estado()).isEqualTo("ACTIVA");
    }

    @Test
    @DisplayName("INACTIVA → ACTIVA: debe lanzar EmpresaInvalidaException si la Empresa está SUSPENDIDA (EMP-06)")
    void ejecutar_sucursalInactiva_conEmpresaSuspendida_lanzaEmpresaInvalidaException() {
        // GIVEN
        Sucursal sucursalInactiva = unaSucursalInactiva();

        when(empresaRepository.buscarPorId(empresaId)).thenReturn(Optional.of(unaEmpresaSuspendida()));
        when(sucursalRepository.buscarPorIdYEmpresaId(sucursalId, empresaId)).thenReturn(Optional.of(sucursalInactiva));

        // WHEN / THEN
        assertThatThrownBy(() -> sut.ejecutar(empresaIdUuid, sucursalIdUuid))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessageContaining("EMP-06");
    }

    @Test
    @DisplayName("Debe lanzar EmpresaNoEncontradaException si la Empresa no existe")
    void ejecutar_conEmpresaInexistente_lanzaEmpresaNoEncontradaException() {
        // GIVEN
        when(empresaRepository.buscarPorId(empresaId)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.ejecutar(empresaIdUuid, sucursalIdUuid))
                .isInstanceOf(EmpresaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Debe lanzar EmpresaInvalidaException si la Sucursal no pertenece a la Empresa (MT-02)")
    void ejecutar_conSucursalDeOtraEmpresa_lanzaEmpresaInvalidaException() {
        // GIVEN — empresa encontrada pero sucursal NO pertenece a ella (MT-02 violation)
        when(empresaRepository.buscarPorId(empresaId)).thenReturn(Optional.of(unaEmpresaActiva()));
        when(sucursalRepository.buscarPorIdYEmpresaId(sucursalId, empresaId)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.ejecutar(empresaIdUuid, sucursalIdUuid))
                .isInstanceOf(EmpresaInvalidaException.class)
                .hasMessageContaining("MT-02");
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si empresaId es null")
    void ejecutar_conEmpresaIdNull_lanzaNullPointerException() {
        assertThatThrownBy(() -> sut.ejecutar(null, sucursalIdUuid))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si sucursalId es null")
    void ejecutar_conSucursalIdNull_lanzaNullPointerException() {
        assertThatThrownBy(() -> sut.ejecutar(empresaIdUuid, null))
                .isInstanceOf(NullPointerException.class);
    }
}
