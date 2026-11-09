package br.com.caminhoremoto.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Depois que a API encerra a sessão dela, manda a pessoa para o Keycloak encerrar o login lá também.
 * Sem isso, o próximo "Entrar" entraria direto, sem pedir a senha.
 * No fim, o Keycloak devolve a pessoa para a página inicial do front.
 */
@Component
public class SaidaDoKeycloak implements LogoutSuccessHandler {

    private final String enderecoDeSaidaDoKeycloak;
    private final String idDoCliente;
    private final String origemDoFront;

    public SaidaDoKeycloak(
            @Value("${keycloak.endereco-de-saida}") String enderecoDeSaidaDoKeycloak,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String idDoCliente,
            @Value("${autenticacao.front.origem}") String origemDoFront) {
        this.enderecoDeSaidaDoKeycloak = enderecoDeSaidaDoKeycloak;
        this.idDoCliente = idDoCliente;
        this.origemDoFront = origemDoFront;
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest pedido, HttpServletResponse resposta, Authentication autenticacao)
            throws IOException {
        resposta.sendRedirect(montarEnderecoDeSaida(autenticacao));
    }

    private String montarEnderecoDeSaida(Authentication autenticacao) {
        UriComponentsBuilder endereco = UriComponentsBuilder.fromUriString(enderecoDeSaidaDoKeycloak)
                .queryParam("client_id", idDoCliente)
                .queryParam("post_logout_redirect_uri", origemDoFront + "/");

        // Com o "id_token_hint", o Keycloak sai direto, sem perguntar "quer mesmo sair?".
        if (autenticacao != null && autenticacao.getPrincipal() instanceof OidcUser usuario) {
            endereco.queryParam("id_token_hint", usuario.getIdToken().getTokenValue());
        }
        return endereco.encode().build().toUriString();
    }
}
