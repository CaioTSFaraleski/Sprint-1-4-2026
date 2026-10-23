package br.com.safefield.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "area_industrial")
public class AreaIndustrial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private String setor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRisco nivelRisco;

    /** Turnos em que a área opera, ex.: "A,B,C". */
    @Column(nullable = false)
    private String turnos;

    @Column(length = 500)
    private String descricao;

    private boolean ativa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private SupervisorSeguranca supervisor;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegraSeguranca> regras = new ArrayList<>();

    protected AreaIndustrial() {}

    public AreaIndustrial(String nome, String setor, NivelRisco nivelRisco, String turnos,
                          String descricao, SupervisorSeguranca supervisor) {
        this.nome = nome;
        this.setor = setor;
        this.nivelRisco = nivelRisco;
        this.turnos = turnos;
        this.descricao = descricao;
        this.supervisor = supervisor;
    }

    /** Regras ativas da área que valem para o turno informado (RF04 / RN01). */
    public List<RegraSeguranca> obterRegrasAtivas(Turno turno) {
        return regras.stream()
                .filter(RegraSeguranca::isObrigatoria)
                .filter(r -> r.getTurno() == null || r.getTurno() == turno)
                .toList();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }
    public NivelRisco getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(NivelRisco nivelRisco) { this.nivelRisco = nivelRisco; }
    public String getTurnos() { return turnos; }
    public void setTurnos(String turnos) { this.turnos = turnos; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public SupervisorSeguranca getSupervisor() { return supervisor; }
    public void setSupervisor(SupervisorSeguranca supervisor) { this.supervisor = supervisor; }
    public List<RegraSeguranca> getRegras() { return regras; }
}
