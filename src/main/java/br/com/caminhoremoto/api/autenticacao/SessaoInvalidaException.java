package br.com.caminhoremoto.api.autenticacao;

public class SessaoInvalidaException extends RuntimeException {

    public SessaoInvalidaException() {
        super("Sua sessão não é mais válida. Entre novamente.");
    }
}
