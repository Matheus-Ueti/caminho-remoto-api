package br.com.caminhoremoto.api.autenticacao;

import br.com.caminhoremoto.api.autenticacao.dto.CadastroRequest;
import br.com.caminhoremoto.api.autenticacao.dto.LoginRequest;
import br.com.caminhoremoto.api.autenticacao.dto.UsuarioResponse;
import br.com.caminhoremoto.api.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final TokenService tokenService;
    private final CookieDeAutenticacao cookieDeAutenticacao;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService,
            TokenService tokenService,
            CookieDeAutenticacao cookieDeAutenticacao) {
        this.autenticacaoService = autenticacaoService;
        this.tokenService = tokenService;
        this.cookieDeAutenticacao = cookieDeAutenticacao;
    }

    /** Cria a conta e já deixa a pessoa logada. */
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest cadastro) {
        Usuario usuario = autenticacaoService.cadastrar(cadastro);
        return responderComLogin(HttpStatus.CREATED, usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> entrar(@Valid @RequestBody LoginRequest login) {
        Usuario usuario = autenticacaoService.autenticar(login);
        return responderComLogin(HttpStatus.OK, usuario);
    }

    @PostMapping("/sair")
    public ResponseEntity<Void> sair() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieDeAutenticacao.apagar().toString())
                .build();
    }

    /** Devolve quem está logado. Só funciona com o cookie de login. */
    @GetMapping("/eu")
    public UsuarioResponse buscarUsuarioLogado(@AuthenticationPrincipal Jwt token) {
        Long idDoUsuario = Long.valueOf(token.getSubject());
        Usuario usuario = autenticacaoService.buscarPorId(idDoUsuario);
        return UsuarioResponse.criarAPartirDe(usuario);
    }

    private ResponseEntity<UsuarioResponse> responderComLogin(HttpStatus status, Usuario usuario) {
        String token = tokenService.gerarToken(usuario);
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookieDeAutenticacao.criar(token).toString())
                .body(UsuarioResponse.criarAPartirDe(usuario));
    }
}
