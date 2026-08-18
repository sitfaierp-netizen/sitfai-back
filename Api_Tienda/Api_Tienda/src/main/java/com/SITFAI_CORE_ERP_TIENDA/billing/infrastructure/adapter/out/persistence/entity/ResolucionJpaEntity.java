package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "billing_resolucion")
public class ResolucionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "empresa_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private String empresaId;

    @Column(name = "prefijo", nullable = false)
    private String prefijo;

    @Column(name = "rango_inicial", nullable = false)
    private long rangoInicial;

    @Column(name = "rango_final", nullable = false)
    private long rangoFinal;

    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDate vigenciaHasta;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    public ResolucionJpaEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }

    public long getRangoInicial() { return rangoInicial; }
    public void setRangoInicial(long rangoInicial) { this.rangoInicial = rangoInicial; }

    public long getRangoFinal() { return rangoFinal; }
    public void setRangoFinal(long rangoFinal) { this.rangoFinal = rangoFinal; }

    public LocalDate getVigenciaHasta() { return vigenciaHasta; }
    public void setVigenciaHasta(LocalDate vigenciaHasta) { this.vigenciaHasta = vigenciaHasta; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
