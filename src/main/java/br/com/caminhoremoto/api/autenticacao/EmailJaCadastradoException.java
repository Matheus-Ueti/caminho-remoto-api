package br.com.caminhoremoto.api.autenticacao;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Este e-mail já está cadastrado.");
    }
}
