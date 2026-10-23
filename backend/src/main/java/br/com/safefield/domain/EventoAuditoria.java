package br.com.safefield.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Registro imutável da trilha de auditoria (RF15 / RNF03). Guarda um retrato textual de autor e área
 * para que o histórico continue legível mesmo que o cadastro mude depois.
 */
@Entity
@Immutable
@Table(name = "evento_auditoria", indexes = @Index(columnList = "dataHora"))
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoAuditoria tipo;

    private Long autorId;

    @Column(nullable = false)
    private String autorNome;

    /** OPERADOR, SUPERVISOR, GESTOR ou SISTEMA. */
    @Column(nullable = false)
    private String autorPerfil;

    private Long areaId;

    private String areaNome;

    private Long ocorrenciaId;

    private String ocorrenciaCodigo;

    @Enumerated(EnumType.STRING)
    private NivelRisco severidade;

    @Column(nullable = false, length = 500)
    private String descricao;

    protected EventoAuditoria() {}

    public EventoAuditoria(LocalDateTime dataHora, TipoEventoAuditoria tipo, Usuario autor, AreaIndustrial area,
                           OcorrenciaRisco ocorrencia, NivelRisco severidade, String descricao) {
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.autorId = autor == null ? null : autor.getId();
        this.autorNome = autor == null ? "SafeField AI" : autor.getNome();
        this.autorPerfil = autor == null ? "SISTEMA" : autor.getPerfil().name();
        this.areaId = area == null ? null : area.getId();
        this.areaNome = area == null ? null : area.getNome();
        this.ocorrenciaId = ocorrencia == null ? null : ocorrencia.getId();
        this.ocorrenciaCodigo = ocorrencia == null ? null : ocorrencia.getCodigo();
        this.severidade = severidade;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public TipoEventoAuditoria getTipo() { return tipo; }
    public Long getAutorId() { return autorId; }
    public String getAutorNome() { return autorNome; }
    public String getAutorPerfil() { return autorPerfil; }
    public Long getAreaId() { return areaId; }
    public String getAreaNome() { return areaNome; }
    public Long getOcorrenciaId() { return ocorrenciaId; }
    public String getOcorrenciaCodigo() { return ocorrenciaCodigo; }
    public NivelRisco getSeveridade() { return severidade; }
    public String getDescricao() { return descricao; }
}
