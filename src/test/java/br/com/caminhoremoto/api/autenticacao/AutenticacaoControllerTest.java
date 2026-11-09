package br.com.caminhoremoto.api.autenticacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoControllerTest {

    private static final String SENHA_VALIDA = "senha-segura-123";
    private static final String ORIGEM_DO_FRONT = "http://localhost:3000";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cadastroCriaContaEDeixaAPessoaLogada() throws Exception {
        MvcResult resultado = cadastrar("Maria", "maria@exemplo.com", SENHA_VALIDA);

        assertThat(resultado.getResponse().getStatus()).isEqualTo(201);
        Cookie cookieDeLogin = resultado.getResponse().getCookie(CookieDeAutenticacao.NOME);
        assertThat(cookieDeLogin).isNotNull();
        assertThat(cookieDeLogin.isHttpOnly()).isTrue();

        mockMvc.perform(get("/auth/eu").cookie(cookieDeLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"))
                .andExpect(jsonPath("$.senhaCriptografada").doesNotExist());
    }

    @Test
    void cadastroComEmailRepetidoRetornaConflito() throws Exception {
        cadastrar("João", "joao@exemplo.com", SENHA_VALIDA);

        // Maiúsculas e minúsculas não mudam o e-mail: continua sendo o mesmo.
        MvcResult resultado = cadastrar("Outro João", "JOAO@exemplo.com", SENHA_VALIDA);

        assertThat(resultado.getResponse().getStatus()).isEqualTo(409);
    }

    @Test
    void cadastroComCamposInvalidosMostraOErroDeCadaCampo() throws Exception {
        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(montarJsonDeCadastro("", "email-invalido", "curta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").value("Informe um e-mail válido."))
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void loginComSenhaCorretaDevolveOCookieDeLogin() throws Exception {
        cadastrar("Ana", "ana@exemplo.com", SENHA_VALIDA);

        MvcResult resultado = entrar("ana@exemplo.com", SENHA_VALIDA);

        assertThat(resultado.getResponse().getStatus()).isEqualTo(200);
        assertThat(resultado.getResponse().getCookie(CookieDeAutenticacao.NOME)).isNotNull();
    }

    @Test
    void loginComSenhaErradaRetornaNaoAutorizado() throws Exception {
        cadastrar("Pedro", "pedro@exemplo.com", SENHA_VALIDA);

        MvcResult resultado = entrar("pedro@exemplo.com", "senha-errada-123");

        assertThat(resultado.getResponse().getStatus()).isEqualTo(401);
        assertThat(resultado.getResponse().getCookie(CookieDeAutenticacao.NOME)).isNull();
    }

    @Test
    void rotaProtegidaSemLoginRetornaNaoAutorizado() throws Exception {
        mockMvc.perform(get("/auth/eu")).andExpect(status().isUnauthorized());
    }

    @Test
    void frontPodeChamarAApiComCookie() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, ORIGEM_DO_FRONT)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_DO_FRONT))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void outroSiteNaoPodeChamarAApi() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://site-estranho.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sairApagaOCookieDeLogin() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/sair")).andReturn();

        assertThat(resultado.getResponse().getStatus()).isEqualTo(204);
        Cookie cookieApagado = resultado.getResponse().getCookie(CookieDeAutenticacao.NOME);
        assertThat(cookieApagado).isNotNull();
        assertThat(cookieApagado.getMaxAge()).isZero();
    }

    private MvcResult cadastrar(String nome, String email, String senha) throws Exception {
        return mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(montarJsonDeCadastro(nome, email, senha)))
                .andReturn();
    }

    private MvcResult entrar(String email, String senha) throws Exception {
        String json = """
                {"email": "%s", "senha": "%s"}
                """.formatted(email, senha);
        return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn();
    }

    private String montarJsonDeCadastro(String nome, String email, String senha) {
        return """
                {"nome": "%s", "email": "%s", "senha": "%s"}
                """.formatted(nome, email, senha);
    }
}
