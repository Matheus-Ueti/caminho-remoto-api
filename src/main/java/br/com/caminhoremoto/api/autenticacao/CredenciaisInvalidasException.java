package br.com.caminhoremoto.api.autenticacao;

public class CredenciaisInvalidasException extends RuntimeException {

    // A mesma mensagem para e-mail inexistente e senha errada, para não revelar quais e-mails existem.
    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}
