package br.com.caminhoremoto.api.vagas;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.caminhoremoto.api.vagas.jooble.ClienteJooble;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VagasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogoDeVagas catalogoDeVagas;

    // Jooble falso, para o teste rodar sem internet.
    @MockitoBean
    private ClienteJooble clienteJooble;

    @BeforeEach
    void prepararVagas() {
        when(clienteJooble.buscarVagas()).thenReturn(List.of(
                new Vaga("jooble-1", "Atendente de Suporte", "Empresa A", "Brasil", "", "", "", ""),
                new Vaga("jooble-2", "Vendedor Online", "Empresa B", "Brasil", "", "", "", "")));
        catalogoDeVagas.atualizarVagas();
    }

    @Test
    void quemNaoEstaLogadoVeAsVagas() throws Exception {
        mockMvc.perform(get("/vagas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void buscaPeloTitulo() throws Exception {
        mockMvc.perform(get("/vagas").param("busca", "vendedor")).andExpect(jsonPath("$[0].id").value("jooble-2"));
    }

    @Test
    void vagaQueNaoExisteDevolve404() throws Exception {
        mockMvc.perform(get("/vagas/nao-existe")).andExpect(status().isNotFound());
    }
}
