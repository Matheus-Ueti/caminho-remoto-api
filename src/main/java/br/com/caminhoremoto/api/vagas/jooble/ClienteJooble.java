package br.com.caminhoremoto.api.vagas.jooble;

import br.com.caminhoremoto.api.vagas.Vaga;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Busca vagas no Jooble, um site que junta vagas de vários outros. Uma busca por palavra-chave. */
@Component
public class ClienteJooble {

    @Value("${jooble.endereco}")
    private String endereco;

    @Value("${jooble.chave}")
    private String chave;

    @Value("${jooble.palavras-chave}")
    private List<String> palavrasChave;

    @Value("${jooble.local}")
    private String local;

    private final RestClient restClient = RestClient.create();

    public List<Vaga> buscarVagas() {
        List<Vaga> vagas = new ArrayList<>();
        if (chave.isBlank()) {
            return vagas;
        }
        for (String palavraChave : palavrasChave) {
            Resposta resposta = restClient.post()
                    .uri(endereco + chave)
                    .body(new Pedido(palavraChave, local))
                    .retrieve()
                    .body(Resposta.class);
            resposta.jobs().forEach(vaga -> vagas.add(converter(vaga)));
        }
        return vagas;
    }

    private static Vaga converter(VagaDoJooble vaga) {
        // O resumo vem com marcações de HTML (<b>, &nbsp;) que o site mostraria como texto.
        String resumo = vaga.snippet().replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
        return new Vaga("jooble-" + vaga.id(), vaga.title(), vaga.company(), vaga.location(),
                vaga.salary(), resumo, vaga.link(), vaga.updated());
    }

    // Formatos que o Jooble usa, com os nomes de campo dele (em inglês).
    private record Pedido(String keywords, String location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Resposta(List<VagaDoJooble> jobs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VagaDoJooble(String id, String title, String company, String location,
            String salary, String snippet, String link, String updated) {
    }
}
