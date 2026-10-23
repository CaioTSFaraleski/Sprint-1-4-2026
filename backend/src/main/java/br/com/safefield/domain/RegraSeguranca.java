package br.com.safefield.domain;

import jakarta.persistence.*;

import java.util.Set;

/** "Na área X, no turno Y, o EPI Z é obrigatório" (RF03 / RF04 / RN01). */
@Entity
@Table(name = "regra_seguranca")
public class RegraSeguranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AreaIndustrial area;

    @ManyToOne(optional = false)
    private EPI epi;

    @Column(nullable = false, length = 300)
    private String descricao;

    private boolean obrigatoria = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRisco severidadePadrao;

    /** Turno em que a regra vale; nulo = todos os turnos. */
    @Enumerated(EnumType.STRING)
    private Turno turno;

    /** Atividade específica (ex.: "Corte a plasma"); nulo = qualquer atividade. */
    private String atividade;

    protected RegraSeguranca() {}

    public RegraSeguranca(AreaIndustrial area, EPI epi, String descricao, NivelRisco severidadePadrao,
                          Turno turno, String atividade) {
        this.area = area;
        this.epi = epi;
        this.descricao = descricao;
        this.severidadePadrao = severidadePadrao;
        this.turno = turno;
        this.atividade = atividade;
    }

    /** A regra é violada quando o tipo de EPI exigido não aparece entre os detectados. */
    public boolean avaliar(Set<TipoEPI> detectados) {
        return detectados.contains(epi.getTipo());
    }

    public Long getId() { return id; }
    public AreaIndustrial getArea() { return area; }
    public EPI getEpi() { return epi; }
    public String getDescricao() { return descricao; }
    public boolean isObrigatoria() { return obrigatoria; }
    public NivelRisco getSeveridadePadrao() { return severidadePadrao; }
    public Turno getTurno() { return turno; }
    public String getAtividade() { return atividade; }
}
