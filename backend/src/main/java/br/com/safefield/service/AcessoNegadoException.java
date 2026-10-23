package br.com.safefield.service;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
