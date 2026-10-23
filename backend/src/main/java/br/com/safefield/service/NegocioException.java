package br.com.safefield.service;

/** Regra de negócio violada (ex.: validar uma ocorrência que não está aguardando validação). */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
