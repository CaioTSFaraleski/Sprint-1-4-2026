package br.com.safefield.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Item do catálogo de EPIs, com o Certificado de Aprovação (CA) do Ministério do Trabalho. */
@Entity
@Table(name = "epi")
public class EPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEPI tipo;

    @Column(nullable = false)
    private String codigoCA;

    @Column(nullable = false)
    private LocalDate validadeCA;

    @Column(length = 500)
    private String observacoes;

    private boolean ativo = true;

    protected EPI() {}

    public EPI(String nome, TipoEPI tipo, String codigoCA, LocalDate validadeCA, String observacoes) {
        this.nome = nome;
        this.tipo = tipo;
        this.codigoCA = codigoCA;
        this.validadeCA = validadeCA;
        this.observacoes = observacoes;
    }

    public boolean validarCertificacao(LocalDate hoje) {
        return !validadeCA.isBefore(hoje);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public TipoEPI getTipo() { return tipo; }
    public String getCodigoCA() { return codigoCA; }
    public LocalDate getValidadeCA() { return validadeCA; }
    public String getObservacoes() { return observacoes; }
    public boolean isAtivo() { return ativo; }
}
