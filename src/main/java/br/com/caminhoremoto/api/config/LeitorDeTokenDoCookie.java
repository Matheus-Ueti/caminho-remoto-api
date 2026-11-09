package br.com.caminhoremoto.api.config;

import br.com.caminhoremoto.api.autenticacao.CookieDeAutenticacao;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

/**
 * Diz ao Spring Security onde procurar o JWT: no cookie de login, e não no cabeçalho Authorization.
 */
public class LeitorDeTokenDoCookie implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest pedido) {
        // Nas rotas públicas ignoramos o cookie. Assim, um login vencido não impede um novo login.
        if (SegurancaConfig.ROTAS_PUBLICAS.contains(pedido.getRequestURI())) {
            return null;
        }

        Cookie[] cookies = pedido.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (CookieDeAutenticacao.NOME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
