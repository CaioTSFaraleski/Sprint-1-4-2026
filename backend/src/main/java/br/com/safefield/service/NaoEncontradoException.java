package br.com.safefield.service;

public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String recurso, Object id) {
        super(recurso + " " + id + " não encontrado(a).");
    }
}
