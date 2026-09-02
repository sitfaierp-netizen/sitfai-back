package com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoTransaccionalTest {

    private DummyDocumento documento;

    @BeforeEach
    void setUp() {
        documento = new DummyDocumento();
    }

    @Test
    @DisplayName("Un documento en BORRADOR debe permitir edición")
    void borradorPermiteEdicion() {
        assertTrue(documento.puedeEditar(), "El documento en BORRADOR debería permitir edición");
    }

    @Test
    @DisplayName("Transición exitosa de BORRADOR a EMITIDO")
    void emitirDocumentoBorrador() {
        assertDoesNotThrow(() -> documento.emitir());
        assertEquals(DocumentStatus.EMITIDO, documento.getEstado(), "El estado debe ser EMITIDO");
        assertFalse(documento.puedeEditar(), "Un documento emitido no debe permitir edición");
    }

    @Test
    @DisplayName("Transición exitosa de EMITIDO a ANULADO")
    void anularDocumentoEmitido() {
        documento.emitir();
        assertDoesNotThrow(() -> documento.anular());
        assertEquals(DocumentStatus.ANULADO, documento.getEstado(), "El estado debe ser ANULADO");
        assertFalse(documento.puedeEditar(), "Un documento anulado no debe permitir edición");
    }

    @Test
    @DisplayName("Emitir un documento ya emitido debe fallar")
    void emitirDocumentoEmitidoFalla() {
        documento.emitir();
        assertThrows(DocumentStateException.class, () -> documento.emitir(), "Debe lanzar DocumentStateException");
    }

    @Test
    @DisplayName("Anular un documento en BORRADOR debe fallar")
    void anularDocumentoBorradorFalla() {
        assertThrows(DocumentStateException.class, () -> documento.anular(), "Debe lanzar DocumentStateException");
    }

    @Test
    @DisplayName("Emitir un documento anulado debe fallar")
    void emitirDocumentoAnuladoFalla() {
        documento.emitir();
        documento.anular();
        assertThrows(DocumentStateException.class, () -> documento.emitir(), "Debe lanzar DocumentStateException");
    }

    @Test
    @DisplayName("Anular un documento ya anulado debe fallar")
    void anularDocumentoAnuladoFalla() {
        documento.emitir();
        documento.anular();
        assertThrows(DocumentStateException.class, () -> documento.anular(), "Debe lanzar DocumentStateException");
    }

    /**
     * Implementación simulada (Dummy) de DocumentoTransaccional para pruebas.
     */
    private static class DummyDocumento implements DocumentoTransaccional {
        private DocumentStatus estado = DocumentStatus.BORRADOR;

        @Override
        public DocumentStatus getEstado() {
            return estado;
        }

        @Override
        public Instant getCreatedAt() {
            return Instant.now();
        }

        @Override
        public String getCreatedBy() {
            return "test-user";
        }

        @Override
        public void cambiarEstado(DocumentStatus nuevoEstado) {
            this.estado = nuevoEstado;
        }
    }
}
