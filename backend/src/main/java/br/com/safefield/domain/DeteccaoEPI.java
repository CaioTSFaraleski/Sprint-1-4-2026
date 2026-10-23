package br.com.safefield.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/** Evento enviado pelo edge gateway (câmera/sensor) com os EPIs reconhecidos em um operador (RF05). */
@Entity
@Table(name = "deteccao_epi", indexes = @Index(columnList = "dataHora"))
public class DeteccaoEPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Operador operador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AreaIndustrial area;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "deteccao_epi_detectado")
    @Column(name = "tipo_epi")
    private Set<TipoEPI> episDetectados = new HashSet<>();

    @Column(precision = 4, scale = 3)
    private BigDecimal confianca;

    private String evidenciaUrl;

    @Enumerated(EnumType.STRING)
    private OrigemDeteccao origem;

    private String dispositivo;

    /** Resultado da avaliação das regras: true = todos os EPIs obrigatórios presentes. */
    private boolean conforme;

    protected DeteccaoEPI() {}

    public DeteccaoEPI(LocalDateTime dataHora, Operador operador, AreaIndustrial area, Set<TipoEPI> episDetectados,
                       BigDecimal confianca, String evidenciaUrl, OrigemDeteccao origem, String dispositivo) {
        this.dataHora = dataHora;
        this.operador = operador;
        this.area = area;
        this.episDetectados = episDetectados.isEmpty() ? new HashSet<>() : new HashSet<>(EnumSet.copyOf(episDetectados));
        this.confianca = confianca;
        this.evidenciaUrl = evidenciaUrl;
        this.origem = origem;
        this.dispositivo = dispositivo;
    }

    public boolean possuiEPIObrigatorio(EPI epi) {
        return episDetectados.contains(epi.getTipo());
    }

    public Long getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public Operador getOperador() { return operador; }
    public AreaIndustrial getArea() { return area; }
    public Set<TipoEPI> getEpisDetectados() { return episDetectados; }
    public BigDecimal getConfianca() { return confianca; }
    public String getEvidenciaUrl() { return evidenciaUrl; }
    public OrigemDeteccao getOrigem() { return origem; }
    public String getDispositivo() { return dispositivo; }
    public boolean isConforme() { return conforme; }
    public void setConforme(boolean conforme) { this.conforme = conforme; }
}
