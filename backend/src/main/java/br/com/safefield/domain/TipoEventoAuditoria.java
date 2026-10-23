package br.com.safefield.domain;

/** Eventos críticos registrados na trilha de auditoria (RF15 / RNF03). */
public enum TipoEventoAuditoria {
    OCORRENCIA_CRIADA,
    ALERTA_EMITIDO,
    REGULARIZACAO_REGISTRADA,
    OCORRENCIA_ESCALADA,
    VALIDACAO_APROVADA,
    VALIDACAO_REJEITADA,
    OCORRENCIA_ENCERRADA,
    COMENTARIO,
    AREA_CRIADA,
    AREA_ATUALIZADA,
    REGRA_CRIADA,
    REGRA_REMOVIDA,
    EPI_CADASTRADO,
    EPI_ENTREGUE,
    USUARIO_CRIADO
}
