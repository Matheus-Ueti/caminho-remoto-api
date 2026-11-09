package br.com.caminhoremoto.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Monta o pedido que manda a pessoa para a tela do Keycloak.
 * Faz o mesmo que o padrão do Spring, com dois ajustes:
 * 1. Usa PKCE, uma proteção extra que o Keycloak exige neste projeto.
 * 2. Com "?cadastro=true" no endereço, o Keycloak abre direto a tela de criar conta.
 */
@Component
public class ResolvedorDoPedidoDeLogin implements OAuth2AuthorizationRequestResolver {

    private static final String PARAMETRO_DE_CADASTRO = "cadastro";

    private final DefaultOAuth2AuthorizationRequestResolver resolvedorPadrao;

    public ResolvedorDoPedidoDeLogin(ClientRegistrationRepository clientesDoLogin) {
        this.resolvedorPadrao = new DefaultOAuth2AuthorizationRequestResolver(
                clientesDoLogin, OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        this.resolvedorPadrao.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest pedido) {
        return ajustarParaCadastro(pedido, resolvedorPadrao.resolve(pedido));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest pedido, String idDoCliente) {
        return ajustarParaCadastro(pedido, resolvedorPadrao.resolve(pedido, idDoCliente));
    }

    private OAuth2AuthorizationRequest ajustarParaCadastro(
            HttpServletRequest pedido, OAuth2AuthorizationRequest pedidoDeLogin) {
        boolean deveAbrirCadastro = pedido.getParameter(PARAMETRO_DE_CADASTRO) != null;
        if (pedidoDeLogin == null || !deveAbrirCadastro) {
            return pedidoDeLogin;
        }
        // "prompt=create" é o jeito padrão de pedir a tela de criar conta ao Keycloak.
        return OAuth2AuthorizationRequest.from(pedidoDeLogin)
                .additionalParameters(parametros -> parametros.put("prompt", "create"))
                .build();
    }
}
