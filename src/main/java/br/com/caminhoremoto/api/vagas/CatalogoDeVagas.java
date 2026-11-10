package br.com.caminhoremoto.api.vagas;

import br.com.caminhoremoto.api.vagas.jooble.ClienteJooble;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Guarda as vagas na memória e as atualiza de tempos em tempos, para o site não depender do Jooble a cada visita.
 * As vagas somem quando a API reinicia, mas voltam na primeira atualização.
 */
@Service
public class CatalogoDeVagas {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogoDeVagas.class);

    private final ClienteJooble clienteJooble;
    private List<Vaga> vagas = List.of();

    public CatalogoDeVagas(ClienteJooble clienteJooble) {
        this.clienteJooble = clienteJooble;
    }

    /** Roda ao subir a API e depois no intervalo definido em application.properties. */
    @Scheduled(initialDelay = 0, fixedDelayString = "${vagas.intervalo-de-atualizacao}")
    public void atualizarVagas() {
        try {
            vagas = clienteJooble.buscarVagas();
            LOG.info("Vagas atualizadas: {}", vagas.size());
        } catch (RuntimeException erro) {
            // Se o Jooble falhar, o site continua mostrando as vagas anteriores.
            LOG.error("Falha ao buscar vagas no Jooble: {}", erro.getMessage());
        }
    }

    public List<Vaga> buscarVagas(String busca) {
        if (busca == null) {
            return vagas;
        }
        String buscaEmMinusculas = busca.toLowerCase();
        return vagas.stream().filter(vaga -> vaga.titulo().toLowerCase().contains(buscaEmMinusculas)).toList();
    }

    public Vaga buscarVagaPorId(String id) {
        return vagas.stream().filter(vaga -> vaga.id().equals(id)).findFirst().orElse(null);
    }
}
