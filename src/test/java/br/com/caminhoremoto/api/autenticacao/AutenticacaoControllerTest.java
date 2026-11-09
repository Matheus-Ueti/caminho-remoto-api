package br.com.caminhoremoto.api.autenticacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoControllerTest {

    private static final String ORIGEM_DO_FRONT = "http://localhost:3000";
    private static final String KEYCLOAK = "http://localhost:8180/realms/caminho-remoto/protocol/openid-connect";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void quemNaoEstaLogadoRecebeNaoAutorizado() throws Exception {
        mockMvc.perform(get("/auth/eu")).andExpect(status().isUnauthorized());
    }

    @Test
    void quemEstaLogadoVeOsPropriosDados() throws Exception {
        mockMvc.perform(get("/auth/eu").with(oidcLogin().idToken(token -> token
                        .subject("id-da-maria")
                        .claim("name", "Maria Silva")
                        .claim("email", "maria@exemplo.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-da-maria"))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"));
    }

    @Test
    void semNomeNoCadastroMostraOEmailNoLugar() throws Exception {
        mockMvc.perform(get("/auth/eu").with(oidcLogin().idToken(token -> token
                        .claim("email", "sem.nome@exemplo.com"))))
                .andExpect(jsonPath("$.nome").value("sem.nome@exemplo.com"));
    }

    @Test
    void entrarLevaParaATelaDoKeycloakComPkce() throws Exception {
        String destino = mockMvc.perform(get("/oauth2/authorization/keycloak"))
                .andExpect(status().isFound())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        assertThat(destino)
                .startsWith(KEYCLOAK + "/auth")
                .contains("client_id=caminho-remoto-api")
                .contains("code_challenge_method=S256")
                .doesNotContain("prompt=create");
    }

    @Test
    void cadastrarAbreATelaDeCriarContaDoKeycloak() throws Exception {
        String destino = mockMvc.perform(get("/oauth2/authorization/keycloak").param("cadastro", "true"))
                .andExpect(status().isFound())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        assertThat(destino).contains("prompt=create");
    }

    @Test
    void sairEncerraOLoginTambemNoKeycloak() throws Exception {
        String destino = mockMvc.perform(get("/auth/sair").with(oidcLogin()))
                .andExpect(status().isFound())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        assertThat(destino)
                .startsWith(KEYCLOAK + "/logout")
                .contains("id_token_hint=")
                .contains("post_logout_redirect_uri=http://localhost:3000/");
    }

    @Test
    void frontPodeChamarAApiComCookie() throws Exception {
        mockMvc.perform(options("/auth/eu")
                        .header(HttpHeaders.ORIGIN, ORIGEM_DO_FRONT)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_DO_FRONT))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void outroSiteNaoPodeChamarAApi() throws Exception {
        mockMvc.perform(options("/auth/eu")
                        .header(HttpHeaders.ORIGIN, "http://site-estranho.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
