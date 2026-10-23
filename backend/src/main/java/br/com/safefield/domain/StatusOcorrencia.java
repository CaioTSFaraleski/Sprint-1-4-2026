package br.com.safefield.domain;

/**
 * Ciclo de vida de uma ocorrência de risco (diagrama de atividades da Sprint 1):
 * ABERTA -> (ESCALADA) -> AGUARDANDO_VALIDACAO -> ENCERRADA.
 * Uma validação rejeitada devolve a ocorrência para ABERTA.
 */
public enum StatusOcorrencia {
    ABERTA, ESCALADA, AGUARDANDO_VALIDACAO, ENCERRADA;

    public boolean ativa() {
        return this != ENCERRADA;
    }
}
