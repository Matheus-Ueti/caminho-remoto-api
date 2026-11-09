package br.com.caminhoremoto.api.autenticacao;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Monta o cookie que guarda o JWT no navegador. */
@Component
public class CookieDeAutenticacao {

    public static final String NOME = "caminho_remoto_token";

    private final Duration validade;
    private final boolean deveSerSeguro;

    public CookieDeAutenticacao(
            @Value("${autenticacao.jwt.validade}") Duration validade,
            @Value("${autenticacao.cookie.seguro}") boolean deveSerSeguro) {
        this.validade = validade;
        this.deveSerSeguro = deveSerSeguro;
    }

    public ResponseCookie criar(String token) {
        return montar(token, validade);
    }

    /** Um cookie vazio com validade zero faz o navegador apagar o cookie de login. */
    public ResponseCookie apagar() {
        return montar("", Duration.ZERO);
    }

    private ResponseCookie montar(String valor, Duration duracao) {
        return ResponseCookie.from(NOME, valor)
                // httpOnly: o JavaScript da página não consegue ler o token.
                .httpOnly(true)
                .secure(deveSerSeguro)
                .sameSite("Lax")
                .path("/")
                .maxAge(duracao)
                .build();
    }
}
