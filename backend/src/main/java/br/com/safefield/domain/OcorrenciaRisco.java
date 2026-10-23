package br.com.safefield.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "ocorrencia_risco", indexes = {@Index(columnList = "dataAbertura"), @Index(columnList = "status")})
public class OcorrenciaRisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private DeteccaoEPI deteccao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Operador operador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AreaIndustrial area;

    @ManyToOne(fetch = FetchType.LAZY)
    private SupervisorSeguranca supervisor;

    @ManyToMany
    @JoinTable(name = "ocorrencia_epi_ausente")
    private Set<EPI> episAusentes = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRisco severidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOcorrencia status = StatusOcorrencia.ABERTA;

    @Column(length = 500)
    private String descricao;

    private boolean reincidente;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    private LocalDateTime dataRegularizacao;

    private LocalDateTime dataEncerramento;

    // Set (e não List): a consulta de detalhe busca ações e EPIs ausentes no mesmo join,
    // e uma List repetiria cada ação uma vez para cada EPI ausente
    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora ASC")
    private Set<AcaoOcorrencia> acoes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataEnvio ASC")
    private List<Alerta> alertas = new ArrayList<>();

    protected OcorrenciaRisco() {}

    public OcorrenciaRisco(String codigo, DeteccaoEPI deteccao, Collection<EPI> episAusentes,
                           NivelRisco severidade, boolean reincidente, String descricao) {
        this.codigo = codigo;
        this.deteccao = deteccao;
        this.operador = deteccao.getOperador();
        this.area = deteccao.getArea();
        this.supervisor = deteccao.getArea().getSupervisor();
        this.episAusentes.addAll(episAusentes);
        this.severidade = severidade;
        this.reincidente = reincidente;
        this.descricao = descricao;
        this.dataAbertura = deteccao.getDataHora();
    }

    /** Código legível (OC-00042), definido logo depois que o banco atribui o id. */
    public void setCodigoSequencial() {
        this.codigo = String.format("OC-%05d", id);
    }

    public AcaoOcorrencia registrarAcao(TipoAcao tipo, Usuario autor, String observacao, LocalDateTime quando) {
        AcaoOcorrencia acao = new AcaoOcorrencia(this, tipo, autor, observacao, quando);
        acoes.add(acao);
        return acao;
    }

    public Alerta emitirAlerta(Usuario destinatario, CanalAlerta canal, String mensagem, LocalDateTime quando) {
        Alerta alerta = new Alerta(this, destinatario, canal, mensagem, quando);
        alertas.add(alerta);
        return alerta;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public DeteccaoEPI getDeteccao() { return deteccao; }
    public Operador getOperador() { return operador; }
    public AreaIndustrial getArea() { return area; }
    public SupervisorSeguranca getSupervisor() { return supervisor; }
    public Set<EPI> getEpisAusentes() { return episAusentes; }
    public NivelRisco getSeveridade() { return severidade; }
    public void setSeveridade(NivelRisco severidade) { this.severidade = severidade; }
    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }
    public String getDescricao() { return descricao; }
    public boolean isReincidente() { return reincidente; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public LocalDateTime getDataRegularizacao() { return dataRegularizacao; }
    public void setDataRegularizacao(LocalDateTime dataRegularizacao) { this.dataRegularizacao = dataRegularizacao; }
    public LocalDateTime getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(LocalDateTime dataEncerramento) { this.dataEncerramento = dataEncerramento; }
    public Set<AcaoOcorrencia> getAcoes() { return acoes; }
    public List<Alerta> getAlertas() { return alertas; }
}
