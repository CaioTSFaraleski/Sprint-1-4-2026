package br.com.safefield.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private OcorrenciaRisco ocorrencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalAlerta canal;

    @Column(nullable = false, length = 300)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAlerta status = StatusAlerta.ENVIADO;

    protected Alerta() {}

    Alerta(OcorrenciaRisco ocorrencia, Usuario destinatario, CanalAlerta canal, String mensagem, LocalDateTime dataEnvio) {
        this.ocorrencia = ocorrencia;
        this.destinatario = destinatario;
        this.canal = canal;
        this.mensagem = mensagem;
        this.dataEnvio = dataEnvio;
    }

    public void marcarComoLido() {
        if (status == StatusAlerta.ENVIADO) status = StatusAlerta.LIDO;
    }

    public Long getId() { return id; }
    public OcorrenciaRisco getOcorrencia() { return ocorrencia; }
    public Usuario getDestinatario() { return destinatario; }
    public CanalAlerta getCanal() { return canal; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public StatusAlerta getStatus() { return status; }
    public void setStatus(StatusAlerta status) { this.status = status; }
}
