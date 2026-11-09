package br.com.caminhoremoto.api.autenticacao;

import br.com.caminhoremoto.api.autenticacao.dto.UsuarioResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrar, criar conta e sair são feitos pelo Keycloak, com rotas do Spring Security (veja SegurancaConfig).
 * Aqui fica só a rota que diz ao front quem está logado.
 */
@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    /** Só responde para quem está logado. Para os outros, a API devolve 401. */
    @GetMapping("/eu")
    public UsuarioResponse buscarUsuarioLogado(@AuthenticationPrincipal OidcUser usuario) {
        return UsuarioResponse.criarAPartirDe(usuario);
    }
}
