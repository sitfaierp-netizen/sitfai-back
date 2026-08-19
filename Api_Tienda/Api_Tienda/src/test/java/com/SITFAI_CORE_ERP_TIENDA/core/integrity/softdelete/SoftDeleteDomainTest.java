package com.SITFAI_CORE_ERP_TIENDA.core.integrity.softdelete;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SoftDeleteDomainTest {

    @Test
    void debeMarcarEmpresaComoInactivaAlDarDeBaja() {
        EmpresaId empresaId = new EmpresaId(UUID.randomUUID());
        Ruc ruc = new Ruc("10203040506");
        Empresa empresa = Empresa.registrar(empresaId, ruc, new com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa("Test Empresa"), null, null);

        assertTrue(empresa.isActivo());
        assertNull(empresa.getDeletedAt());
        assertNull(empresa.getDeletedBy());

        String actorId = UUID.randomUUID().toString();
        empresa.darDeBaja(actorId);

        assertFalse(empresa.isActivo());
        assertNotNull(empresa.getDeletedAt());
        assertEquals(actorId, empresa.getDeletedBy());
    }
}
