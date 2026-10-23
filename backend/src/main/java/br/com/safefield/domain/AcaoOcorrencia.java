package br.com.safefield.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "acao_ocorrencia")
public class AcaoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private OcorrenciaRisco ocorrencia;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAcao tipo;

    /** Nulo quando a ação foi executada pelo próprio sistema. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario autor;

    @Column(length = 500)
    private String observacao;

    protected AcaoOcorrencia() {}

    AcaoOcorrencia(OcorrenciaRisco ocorrencia, TipoAcao tipo, Usuario autor, String observacao, LocalDateTime dataHora) {
        this.ocorrencia = ocorrencia;
        this.tipo = tipo;
        this.autor = autor;
        this.observacao = observacao;
        this.dataHora = dataHora;
    }

    public Long getId() { return id; }
    public OcorrenciaRisco getOcorrencia() { return ocorrencia; }
    public LocalDateTime getDataHora() { return dataHora; }
    public TipoAcao getTipo() { return tipo; }
    public Usuario getAutor() { return autor; }
    public String getObservacao() { return observacao; }
}
