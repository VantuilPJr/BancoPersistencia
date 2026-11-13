package br.com.banco.service;

public class NegocioException extends RuntimeException {
    private final boolean naoEncontrado;

    public NegocioException(String message) {
        this(message, false);
    }

    public NegocioException(String message, boolean naoEncontrado) {
        super(message);
        this.naoEncontrado = naoEncontrado;
    }

    public boolean isNaoEncontrado() {
        return naoEncontrado;
    }
}
