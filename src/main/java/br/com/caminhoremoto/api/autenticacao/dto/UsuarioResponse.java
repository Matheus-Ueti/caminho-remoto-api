package br.com.caminhoremoto.api.autenticacao.dto;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * O que a API devolve sobre quem está logado.
 * O "id" é o identificador da pessoa no Keycloak (um texto, não um número).
 */
public record UsuarioResponse(String id, String nome, String email) {

    public static UsuarioResponse criarAPartirDe(OidcUser usuario) {
        return new UsuarioResponse(usuario.getSubject(), escolherNome(usuario), usuario.getEmail());
    }

    // Se a pessoa não informou o nome no cadastro, mostramos o e-mail no lugar.
    private static String escolherNome(OidcUser usuario) {
        String nomeCompleto = usuario.getFullName();
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return usuario.getEmail();
        }
        return nomeCompleto;
    }
}
