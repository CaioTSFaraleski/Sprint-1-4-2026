package br.com.safefield.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Ficha de entrega de EPI ao colaborador (registro de EPIs por colaborador). */
@Entity
@Table(name = "entrega_epi")
public class EntregaEPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Operador operador;

    @ManyToOne(optional = false)
    private EPI epi;

    @Column(nullable = false)
    private LocalDate dataEntrega;

    @Column(nullable = false)
    private LocalDate dataValidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEntrega status = StatusEntrega.ATIVA;

    protected EntregaEPI() {}

    public EntregaEPI(Operador operador, EPI epi, LocalDate dataEntrega, LocalDate dataValidade) {
        this.operador = operador;
        this.epi = epi;
        this.dataEntrega = dataEntrega;
        this.dataValidade = dataValidade;
    }

    public StatusEntrega statusEm(LocalDate hoje) {
        if (status == StatusEntrega.DEVOLVIDA) return status;
        return dataValidade.isBefore(hoje) ? StatusEntrega.VENCIDA : StatusEntrega.ATIVA;
    }

    public Long getId() { return id; }
    public Operador getOperador() { return operador; }
    public EPI getEpi() { return epi; }
    public LocalDate getDataEntrega() { return dataEntrega; }
    public LocalDate getDataValidade() { return dataValidade; }
    public StatusEntrega getStatus() { return status; }
    public void setStatus(StatusEntrega status) { this.status = status; }
}
